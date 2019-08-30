package com.xMarket.matching;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xMarket.matching.Model.*;
import com.xMarket.matching.Repository.StockRepository;
import com.xMarket.matching.config.RabbitConfig;
import com.xMarket.matching.pool.AllocOnlyPool;
import com.xMarket.matching.pool.RecyclablePool;
import com.xMarket.matching.pool.TradeOrderPool;
import com.xMarket.matching.redis.RealTime1Redis;
import com.xMarket.model.Gear;
import com.xMarket.model.RealTime1;

import java.text.DecimalFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;


public class Matcher {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private RealTime1Redis stockRedis;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //股票撮合列表
    private TradedInstList stockList;
    //价格档位对象池
    private RecyclablePool prcLdrPool;
    //订单对象池
    private AllocOnlyPool orderPool;
    //成交单对象池
    private TradeOrderPool tradeOrderPool;
    //删除链表
    private LinkedList<Double> delPrcLdrList;
    private LinkedList<Double> delPrcLdrList2;
    //订单列表 订单id 订单
    private TreeMap<Long, Morder> orderList;
    //构造函数

    public Matcher() throws InstantiationException, IllegalAccessException {
        this.stockList = new TradedInstList();
        this.prcLdrPool = new RecyclablePool(1000);
        this.orderPool = new AllocOnlyPool(10000);
        this.tradeOrderPool = new TradeOrderPool(10000);
        this.delPrcLdrList = new LinkedList<Double>();
        this.delPrcLdrList2 = new LinkedList<Double>();
        this.orderList = new TreeMap<Long, Morder>();
    }

    public Matcher(int prcLdrCnt, int orderCnt, int tradeOrderCnt)
            throws InstantiationException, IllegalAccessException {
        this.stockList = new TradedInstList();
        this.prcLdrPool = new RecyclablePool(prcLdrCnt);
        this.orderPool = new AllocOnlyPool(orderCnt);
        this.tradeOrderPool = new TradeOrderPool(tradeOrderCnt);
        this.delPrcLdrList = new LinkedList<Double>();
        this.delPrcLdrList2 = new LinkedList<Double>();
        this.orderList = new TreeMap<Long, Morder>();
    }


    @RabbitListener(queues = RabbitConfig.QUEUE_B)
    public void delOrder(String content) {
        logger.info("接收处理撤单队列当中的消息： " + content);
        delOrder(Long.parseLong(content));

    }


    @RabbitListener(queues = RabbitConfig.QUEUE_D)
    public void allo(String content) {
        logger.info("接收处理集合竞价委托队列当中的消息： " + content);
        JSONObject order = JSON.parseObject(content);
        Morder morder = new Morder(order);
        insertOrder(morder);
    }


    @RabbitListener(queues = RabbitConfig.QUEUE_A)
    public void process(String content) {
        logger.info("接收处理连续竞价委托队列当中的消息： " + content);
        JSONObject order = JSON.parseObject(content);
        Morder morder = new Morder(order);
        continuousBidding(morder);

    }

    @PostConstruct
    public void init() {

        //utils.stopMessageListener(RabbitConfig.QUEUE_A);
        logger.info("停止所有队列监听");

        List<TradedInst> temp = stockRepository.findAll();
        Iterator<TradedInst> iterator = temp.iterator();
        TradedInst stock = null;
        while (iterator.hasNext()) {
            stock = iterator.next();
            TradedInst stock1 = new TradedInst(stock.getStockId(), stock.getStockname(), stock.getPastClosePrice());
            stockList.addStock(stock1);

            logger.info("初始化股票信息： " + stock.getStockId() + " " + stock.getStockname());

            stock1.setNew_price(stock.getPastClosePrice());
            RealTime1 real = getRealTime1(stock1);
            stockRedis.put(stock.getStockId() + "", real, -1);
            logger.info("初始化股票: " + stock.getStockId() + " 实时信息");
        }
    }

    //买五
    public List<Gear> getBuyFive(TradedInst stock) {
        List<Gear> list = new LinkedList<Gear>();
        int level = 0;
        Iterator<Map.Entry<Double, PriceLeader>> itsB = stock.getPrcList(0).entrySet().iterator();
        while (itsB.hasNext()) {
            PriceLeader priceLeader = itsB.next().getValue();
            if (priceLeader.getAccumQty() <= 0)
                continue;
            list.add(new Gear(priceLeader.getPrice(), priceLeader.getAccumQty()));
            level++;
            if (level >= 5)
                break;
        }
        for (int i = level; i < 5; i++)
            list.add(new Gear(-1.0, -1));
        logger.info("获取股票：" + stock.getStockname() + "买方五档");
        return list;
    }

    //卖五
    public List<Gear> getSellFive(TradedInst stock) {
        List<Gear> list = new LinkedList<Gear>();
        int level = 0;
        Iterator<Map.Entry<Double, PriceLeader>> itsS = stock.getPrcList(1).entrySet().iterator();
        while (itsS.hasNext()) {
            PriceLeader priceLeader = itsS.next().getValue();
            if (priceLeader.getAccumQty() <= 0)
                continue;
            list.add(new Gear(priceLeader.getPrice(), priceLeader.getAccumQty()));
            level++;
            if (level >= 5)
                break;
        }
        for (int i = level; i < 5; i++)
            list.add(new Gear(-1.0, -1));
        logger.info("获取股票：" + stock.getStockname() + "卖方五档");
        return list;
    }

    //申请订单对象
    public final Morder allocOrder() {
        logger.info("申请订单对象");
        return orderPool.getObj();
    }

    //申请成交单对象
    public final MtradeOrder allocTradeOrder() {
        logger.info("申请成交单对象");
        return tradeOrderPool.getObj();
    }

    //获取本方价格档位
    public PriceLeader getPrcLdr(Morder order) {
        TradedInst stock = stockList.getStock(order.getStock_id());
        TreeMap<Double, PriceLeader> prcList = stock.getPrcList(order.getType());
        PriceLeader priceLeader = null;
        if (order.getType() == 0)
            priceLeader = prcList.get(-order.getOrder_price());
        else
            priceLeader = prcList.get(order.getOrder_price());
        logger.info("获取订单所在价格档位");
        return priceLeader;
    }

    //撤单
    public final boolean delOrder(long orderId) {
        if (orderId <= 0) {
            logger.error("传入订单id小于0");
            return false;
        }
        Morder order = orderList.get(orderId);
        if (order == null) {
            logger.info("委托队列中不存在该订单");
            return false;
        }
        order.setDelflg(true);
        logger.info("订单撤单位置真");
        TradedInst stock = stockList.getStock(order.getStock_id());
        PriceLeader priceLeader = getPrcLdr(order);
        priceLeader.setAccumQty(priceLeader.getAccumQty() - order.getRemQty());
        if (priceLeader.getAccumQty() <= 0) {
            if (priceLeader.getIsbuy() == 0)
                delPrcLdrList.add(priceLeader.getPrior());
            else
                delPrcLdrList2.add(priceLeader.getPrior());
        }
        orderList.remove(orderId);
        MtradeOrder tradeOrder = null;
        if (order.getType() == 0)
            tradeOrder = getTradeOrder(order.getStock_id(), orderId, -1, true, false,
                    -1, order.getRemQty(), -1, order.getOwner(), -1);
        else
            tradeOrder = getTradeOrder(order.getStock_id(), -1, orderId, false, true,
                    -1, order.getRemQty(), -1, -1, order.getOwner());

        logger.info("生成撤单信息: " + tradeOrder.toJson());

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_B, RabbitConfig.ROUTINGKEY_C, tradeOrder.toJson());
        logger.info("发送撤单消息至消息队列");
        RealTime1 real = getRealTime1(stock);
        stockRedis.put(stock.getStockId() + "", real, -1);
        logger.info("更新股票行情揭露信息");
        removePrcLdr(stock);
        return true;
    }

    public boolean record(TradedInst stock, MtradeOrder order) {
        if (stock == null || order == null) {
            logger.error("传入股票为空或交易单为空");
            return false;
        }

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_B, RabbitConfig.ROUTINGKEY_C, order.toJson());
        logger.info("发送交易单至消息队列");
        update(stock, order);
        logger.info("更新股票实时信息");
        RealTime1 real = getRealTime1(stock);
        stockRedis.put(stock.getStockId() + "", real, -1);
        logger.info("更新股票行情揭露信息");
        return true;
    }

    //插入订单簿
    private final boolean insertOrder(Morder order) {
        if (order.getRemQty() <= 0) {
            logger.info("订单剩余委托量小于或等于0，无法插入订单簿");
            return false;
        }
        TradedInst stock = stockList.getStock(order.getStock_id());
        PriceLeader priceLeader = getPrcLdr(order);
        if (priceLeader == null) {
            priceLeader = prcLdrPool.getObj();
            if (priceLeader == null)
                return false;
            priceLeader.setAccumQty(order.getRemQty());
            priceLeader.setIsbuy(order.getType());
            priceLeader.getOrderlist().add(order);
            priceLeader.setPrice(order.getOrder_price());
            if (order.getType() == 0) {
                priceLeader.setPrior(-order.getOrder_price());
                stock.addtoPrcList(true, priceLeader);
            } else {
                priceLeader.setPrior(order.getOrder_price());
                stock.addtoPrcList(false, priceLeader);
            }
        } else {
            priceLeader.setAccumQty(priceLeader.getAccumQty() + order.getRemQty());
            priceLeader.getOrderlist().add(order);
        }
        orderList.put(order.getOrder_id(), order);
        RealTime1 real = getRealTime1(stock);
        stockRedis.put(stock.getStockId() + "", real, -1);
        logger.info("更新股票行情揭露信息");
        return true;
    }

    //生成成交单
    public MtradeOrder getTradeOrder(String stockID, long buyOrderId, long sellOrderId,
                                     boolean buyPoint, boolean sellPoint, double tradePrice,
                                     int exchangeAmount, int tradeType, int buyerId, int sellerId) {
        MtradeOrder mtradeOrder = allocTradeOrder();
        String id = "";


        if (buyOrderId == -1)
            id = "-" + id + sellOrderId;
        else if (sellOrderId == -1)
            id = "-" + id + buyOrderId;
        else {
            TradedInst stock = stockList.getStock(stockID);
            stock.setNew_price(tradePrice);

        }
        mtradeOrder.setTradeOrderId(System.currentTimeMillis());
        mtradeOrder.setStockID(stockID);
        mtradeOrder.setBuyOrderId(buyOrderId);
        mtradeOrder.setSellOrderId(sellOrderId);
        mtradeOrder.setBuyPoint(buyPoint);
        mtradeOrder.setSellPoint(sellPoint);
        mtradeOrder.setExchangeAmount(exchangeAmount);
        mtradeOrder.setTradeType(tradeType);
        mtradeOrder.setDate(new Date());
        mtradeOrder.setTradePrice(tradePrice);
        mtradeOrder.setBuyerId(buyerId);
        mtradeOrder.setSellerId(sellerId);

        return mtradeOrder;
    }

    public int max(int a, int b) {
        if (a > b)
            return a;
        else
            return b;
    }

    public int min(int a, int b) {
        if (a < b)
            return a;
        else
            return b;
    }

    //计算集合竞价成交价，不进行撮合
    private CallAuctionResult calcCallAuction(TradedInst stock) {
        CallAuctionResult result = new CallAuctionResult();
        TreeMap<Double, PriceLeader> buyTree = null;
        TreeMap<Double, PriceLeader> sellTree = null;
        Map.Entry<Double, PriceLeader> buyEntry = null;
        Map.Entry<Double, PriceLeader> sellEntry = null;
        PriceLeader buyLdr = null;
        PriceLeader sellLdr = null;
        Double buyKey = null;
        Double sellKey = null;

        if (stock == null) {
            logger.error("该股票不存在，撮合失败");
            return null;
        }
        buyTree = stock.getPrcList(0);
        if (buyTree == null) {
            logger.error("该股票买方队列未初始化，撮合失败");
            return null;
        }
        sellTree = stock.getPrcList(1);
        if (sellTree == null) {
            logger.error("该股票卖方队列未初始化，撮合失败");
            return null;
        }

        buyEntry = buyTree.firstEntry();
        sellEntry = sellTree.firstEntry();
        if (buyEntry == null || sellEntry == null) {
            result.setPrice(-1);
            result.setVolume(0);
            return result;
        } else {
            buyKey = buyEntry.getKey();
            buyLdr = buyEntry.getValue();

            sellKey = sellEntry.getKey();
            sellLdr = sellEntry.getValue();

            if (sellLdr.getPrice() > buyLdr.getPrice()) {
                result.setPrice(-1);
                result.setVolume(0);
                return result;
            }

            // 从买卖队列的头开始遍历
            int totalMatchedQty = 0;
            double lastBuyPrice = 0;
            double lastSellPrice = 0;
            int buyQtyRemain = buyLdr.getAccumQty();
            int sellQtyRemain = sellLdr.getAccumQty();

            while (buyLdr.getPrice() >= sellLdr.getPrice()) {
                int matchvolum = min(buyQtyRemain, sellQtyRemain);
                totalMatchedQty += matchvolum;
                buyQtyRemain -= matchvolum;
                sellQtyRemain -= matchvolum;

                lastBuyPrice = buyLdr.getPrice();
                lastSellPrice = sellLdr.getPrice();
                if (buyQtyRemain == 0) {
                    buyEntry = stock.getPrcList(0).higherEntry(buyKey);
                    if (buyEntry == null)
                        break;
                    else {
                        buyKey = buyEntry.getKey();
                        buyLdr = buyEntry.getValue();
                        buyQtyRemain = buyLdr.getAccumQty();
                    }
                } else {
                    sellEntry = stock.getPrcList(1).higherEntry(sellKey);
                    if (sellEntry == null)
                        break;
                    else {
                        sellKey = sellEntry.getKey();
                        sellLdr = sellEntry.getValue();
                        sellQtyRemain = sellLdr.getAccumQty();
                    }
                }
            }
            if (stock.getStockId().startsWith("600")) {
                DecimalFormat df = new DecimalFormat("#.00");
                result.setPrice(Double.parseDouble(df.format((lastBuyPrice + lastSellPrice) / 2)));
                result.setVolume(totalMatchedQty);
                return result;
            } else {
                result.setVolume(totalMatchedQty);
                if (lastBuyPrice == lastSellPrice) {
                    result.setPrice((lastBuyPrice + lastSellPrice) / 2);
                    return result;
                } else {
                    int a = 0, b = 0, c = 0, d = 0;
                    Map.Entry<Double, PriceLeader> temp = buyTree.firstEntry();
                    while (temp.getValue().getPrice() > lastBuyPrice) {
                        a += temp.getValue().getAccumQty();
                        temp = buyTree.higherEntry(temp.getKey());
                        if (temp == null)
                            break;
                    }
                    temp = sellTree.firstEntry();
                    while (temp.getValue().getPrice() < lastBuyPrice) {
                        b += temp.getValue().getAccumQty();
                        temp = sellTree.higherEntry(temp.getKey());
                        if (temp == null)
                            break;
                    }
                    temp = buyTree.firstEntry();
                    while (temp.getValue().getPrice() > lastSellPrice) {
                        c += temp.getValue().getAccumQty();
                        temp = buyTree.higherEntry(temp.getKey());
                        if (temp == null)
                            break;
                    }
                    temp = sellTree.firstEntry();
                    while (temp.getValue().getPrice() < lastSellPrice) {
                        d += temp.getValue().getAccumQty();
                        temp = sellTree.higherEntry(temp.getKey());
                        if (temp == null)
                            break;
                    }
                    int p1 = Math.abs(a - b);
                    int p2 = Math.abs(c - d);
                    if (p1 < p2)
                        result.setPrice(lastBuyPrice);
                    else if (p1 > p2)
                        result.setPrice(lastSellPrice);
                    else {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        if (hour < 12)
                            result.setPrice(stock.getPastClosePrice());
                        else
                            result.setPrice(stock.getNew_price());
                    }
                    return result;
                }
            }
        }
    }


    //集合竞价撮合
    @Scheduled(cron = "0 25 9 ? * MON-FRI")
    @Scheduled(cron = "0 00 22 ? * MON-FRI")
    public boolean doCallAuction() {
        Iterator<Map.Entry<String, TradedInst>> its = stockList.getList().entrySet().iterator();
        while (its.hasNext()) {
            TradedInst stock = its.next().getValue();
            CallAuctionResult result = calcCallAuction(stock);
            if (stock == null || result == null) {
                logger.error("此股票集合竞价撮合失败");
                continue;
            }

            if (result.getVolume() <= 0) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                //改回12
                if (hour < 12) {
                    stock.setNew_price(stock.getPastClosePrice());
                    stock.setOpenPrice(stock.getNew_price());
                } else
                    stock.setClosePrice(stock.getNew_price());
                RealTime1 real = getRealTime1(stock);
                stockRedis.put(stock.getStockId() + "", real, -1);
                logger.info("更新股票行情揭露信息");
                continue;
            } else {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour < 12) {
                    stock.setOpenPrice(result.getPrice());
                } else
                    stock.setClosePrice(result.getPrice());
            }

            PriceLeader buyLdr = null;
            PriceLeader sellLdr = null;
            Morder buyOrd = null;
            Morder sellOrd = null;

            int remainQty = result.getVolume();
            Iterator<Map.Entry<Double, PriceLeader>> itsB = stock.getPrcList(0).entrySet().iterator();
            Iterator<Map.Entry<Double, PriceLeader>> itsS = stock.getPrcList(1).entrySet().iterator();

            if (!itsB.hasNext())
                return false;
            else
                buyLdr = itsB.next().getValue();

            if (!itsS.hasNext())
                return false;
            else
                sellLdr = itsS.next().getValue();

            boolean nextBuyOrd = true;
            boolean nextSellOrd = true;
            int qty = 0;
            MtradeOrder tradeOrder;
            LinkedList<MtradeOrder> tradeList = new LinkedList<MtradeOrder>();
            while (remainQty > 0) {
                while (nextBuyOrd) {
                    buyOrd = buyLdr.getOrderlist().poll();
                    if (buyOrd == null) {
                        if ((!itsB.hasNext()))
                            return false;
                        else {
                            buyLdr = itsB.next().getValue();
                            continue;
                        }
                    } else {
                        if (buyOrd.isDelflg())
                            continue;
                        break;
                    }


                }

                while (nextSellOrd) {
                    sellOrd = sellLdr.getOrderlist().poll();
                    if (sellOrd == null) {
                        if ((!itsS.hasNext()))
                            return false;
                        else {
                            sellLdr = itsS.next().getValue();
                            continue;
                        }
                    } else {
                        if (sellOrd.isDelflg())
                            continue;
                        break;
                    }
                }

                qty = min(buyOrd.getRemQty(), sellOrd.getRemQty());
                qty = min(qty, remainQty);
                buyOrd.setRemQty(buyOrd.getRemQty() - qty);
                buyLdr.setAccumQty(buyLdr.getAccumQty() - qty);

                if (buyLdr.getAccumQty() <= 0)
                    delPrcLdrList.add(buyLdr.getPrior());

                sellOrd.setRemQty(sellOrd.getRemQty() - qty);
                sellLdr.setAccumQty(sellLdr.getAccumQty() - qty);

                if (sellLdr.getAccumQty() <= 0)
                    delPrcLdrList2.add(sellLdr.getPrior());

                remainQty -= qty;

                tradeOrder = getTradeOrder(buyOrd.getStock_id(), buyOrd.getOrder_id(), sellOrd.getOrder_id(),
                        false, false, result.getPrice(), qty, -1, buyOrd.getOwner(), sellOrd.getOwner());

                if (buyOrd.getRemQty() <= 0) {
                    nextBuyOrd = true;
                    tradeOrder.setBuyPoint(true);
                } else {
                    nextBuyOrd = false;
                    tradeOrder.setBuyPoint(false);
                }
                if (sellOrd.getRemQty() <= 0) {
                    nextSellOrd = true;
                    tradeOrder.setSellPoint(true);
                } else {
                    nextSellOrd = false;
                    tradeOrder.setSellPoint(false);
                }
                logger.info("生成成交单：" + tradeOrder.toJson());
                record(stock, tradeOrder);

            }
            if (buyOrd.getRemQty() > 0)
                getPrcLdr(buyOrd).getOrderlist().add(buyOrd);
            if (sellOrd.getRemQty() > 0)
                getPrcLdr(sellOrd).getOrderlist().add(sellOrd);
            removePrcLdr(stock);
        }
        return true;
    }

    //获取即时信息
    public RealTime1 getRealTime1(TradedInst stock) {
        return new RealTime1(stock.getStockId(), stock.getNew_price(), stock.getOpenPrice(), stock.getMaxPrice(),
                stock.getMinPrice(), stock.getTradeVolumn(), stock.getPastClosePrice(), stock.getTradeAmount(),
                stock.getClosePrice(), getBuyFive(stock), getSellFive(stock), stock.getInvol(), stock.getOuterDisc());
    }

    //计算成交量
    public int match(Morder order, Iterator<Map.Entry<Double, PriceLeader>> its) {
        int qty = 0;
        PriceLeader priceLeader = null;
        int level = 0;
        TradedInst stock = stockList.getStock(order.getStock_id());
        if (order.getTrade_straregy() == 1 || order.getTrade_straregy() == 2 || order.getTrade_straregy() == 5
                || order.getTrade_straregy() == 6 || order.getTrade_straregy() == 7) {
            if (order.getType() == 0)
                order.setOrder_price(Double.MAX_VALUE);
            else
                order.setOrder_price(Double.MIN_VALUE);
        } else if (order.getTrade_straregy() == 3) {
            if (stock.getBestPeerPrcLdr(order.getType()) == null)
                return -1;
            PriceLeader bestLeader = stock.getBestPeerPrcLdr(order.getType()).getValue();
            order.setOrder_price(bestLeader.getPrice());
        } else if (order.getTrade_straregy() == 4) {
            if (order.getType() == 0) {
                if (stock.getBestPeerPrcLdr(1) == null)
                    return -1;
            } else {
                if (stock.getBestPeerPrcLdr(0) == null)
                    return -1;
            }
            PriceLeader bestLeader = null;
            if (order.getType() == 0)
                bestLeader = stock.getBestPeerPrcLdr(1).getValue();
            else
                bestLeader = stock.getBestPeerPrcLdr(0).getValue();

            order.setOrder_price(bestLeader.getPrice());
        } else
            order.setOrder_price(order.getOrder_price());
        while (its.hasNext() && qty < order.getRemQty()) {
            priceLeader = its.next().getValue();
            if (order.getType() == 0) {
                if (order.getOrder_price() >= priceLeader.getPrice())
                    qty += min(order.getRemQty() - qty, priceLeader.getAccumQty());
                else
                    break;
            } else {
                if (order.getOrder_price() <= priceLeader.getPrice())
                    qty += min(order.getRemQty() - qty, priceLeader.getAccumQty());
                else
                    break;
            }
            level++;
            if (order.getTrade_straregy() == 1 || order.getTrade_straregy() == 2 || order.getTrade_straregy() == 5)
                if (level == 5)
                    break;
        }
        return qty;
    }

    //匹配成交
    public boolean doBdding(Morder order, int qty, Iterator<Map.Entry<Double, PriceLeader>> its) {
        delPrcLdrList.clear();
        delPrcLdrList2.clear();
        if (order == null || qty <= 0 || its == null)
            return false;
        PriceLeader priceLeader = null;
        Morder oldOrder = null;
        boolean nextOrder = true;
        MtradeOrder tradeOrder = null;
        if (!its.hasNext())
            return false;
        else
            priceLeader = its.next().getValue();
        int nnqty = 0;
        TradedInst stock = stockList.getStock(order.getStock_id());
        while (qty > 0) {
            while (nextOrder) {
                oldOrder = priceLeader.getOrderlist().poll();
                if (oldOrder == null) {
                    if ((!its.hasNext()))
                        return false;
                    else {
                        priceLeader = its.next().getValue();
                        continue;
                    }
                } else {
                    if (oldOrder.isDelflg())
                        continue;
                    break;
                }
            }
            nnqty = min(order.getRemQty(), oldOrder.getRemQty());

            order.setRemQty(order.getRemQty() - nnqty);
            oldOrder.setRemQty(oldOrder.getRemQty() - nnqty);
            priceLeader.setAccumQty(priceLeader.getAccumQty() - nnqty);
            if (priceLeader.getAccumQty() <= 0) {

                if (order.getType() == 0)
                    delPrcLdrList2.add(priceLeader.getPrior());
                else
                    delPrcLdrList.add(priceLeader.getPrior());
            }
            qty -= nnqty;

            if (order.getType() == 0) {
                tradeOrder = getTradeOrder(order.getStock_id(), order.getOrder_id(), oldOrder.getOrder_id(),
                        false, false, oldOrder.getOrder_price(),
                        nnqty, 0, order.getOwner(), oldOrder.getOwner());

                if (order.getRemQty() <= 0)
                    tradeOrder.setBuyPoint(true);
                else
                    tradeOrder.setBuyPoint(false);
                if (oldOrder.getRemQty() <= 0) {
                    nextOrder = true;
                    tradeOrder.setSellPoint(true);
                } else {
                    nextOrder = false;
                    tradeOrder.setSellPoint(false);
                }
            } else {
                tradeOrder = getTradeOrder(order.getStock_id(), oldOrder.getOrder_id(), order.getOrder_id(),
                        false, false, oldOrder.getOrder_price(),
                        nnqty, 1, oldOrder.getOwner(), order.getOwner());
                if (oldOrder.getRemQty() <= 0) {
                    nextOrder = true;
                    tradeOrder.setBuyPoint(true);
                } else {
                    nextOrder = false;
                    tradeOrder.setBuyPoint(false);
                }
                if (order.getRemQty() <= 0) {
                    tradeOrder.setSellPoint(true);
                } else
                    tradeOrder.setSellPoint(false);
            }
            logger.info("生成成交单：" + tradeOrder.toJson());
            record(stock, tradeOrder);

        }
        if (oldOrder.getRemQty() > 0)
            getPrcLdr(oldOrder).getOrderlist().add(oldOrder);
        removePrcLdr(stock);
        return true;
    }

    //更新实时信息
    public void update(TradedInst stock, MtradeOrder order) {
        stock.setNew_price(order.getTradePrice());
        stock.setTradeVolumn(stock.getTradeVolumn() + order.getExchangeAmount());
        stock.setTradeAmount(stock.getTradeAmount() + order.getExchangeAmount() * order.getTradePrice());
        if (order.getTradePrice() > stock.getMaxPrice())
            stock.setMaxPrice(order.getTradePrice());
        if (order.getTradePrice() < stock.getMinPrice())
            stock.setMinPrice(order.getTradePrice());
        if (order.getTradeType() == 0)
            stock.setOuterDisc(stock.getOuterDisc() + order.getExchangeAmount());
        else
            stock.setInvol(stock.getInvol() + order.getExchangeAmount());
    }

    //移除档位
    public void removePrcLdr(TradedInst stock) {
        Iterator<Double> myIter = delPrcLdrList.iterator();
        PriceLeader rmvLdr;
        while (myIter.hasNext()) {
            double pri = myIter.next();
            rmvLdr = stock.getPrcList(0).remove(pri);
            prcLdrPool.putObj(rmvLdr);
        }

        myIter = delPrcLdrList2.iterator();
        while (myIter.hasNext()) {
            double pri = myIter.next();
            rmvLdr = stock.getPrcList(1).remove(pri);
            prcLdrPool.putObj(rmvLdr);
        }
        delPrcLdrList.clear();
        delPrcLdrList2.clear();
    }

    public void backOrder(Morder order) {
        if (order == null)
            return;
        order.setDelflg(true);
        MtradeOrder mtradeOrder = null;
        if (order.getType() == 0)
            mtradeOrder = getTradeOrder(order.getStock_id(), order.getOrder_id(), -1, true,
                    false, -1, order.getRemQty(), -1, order.getOwner(), -1);
        else
            mtradeOrder = getTradeOrder(order.getStock_id(), -1, order.getOrder_id(), false,
                    true, -1, order.getRemQty(), -1, -1, order.getOwner());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_B, RabbitConfig.ROUTINGKEY_C, mtradeOrder.toJson());
        /**
         * 记录成交单
         */
    }

    //连续竞价
    public boolean continuousBidding(Morder order) {
        order.setRemQty(order.getOrder_amount());
        TradedInst stock = stockList.getStock(order.getStock_id());
        if (stock == null)
            return false;
        TreeMap<Double, PriceLeader> matchTree = stock.getPeerPrcTree(order.getType());
        int qty = match(order, matchTree.entrySet().iterator());
        LinkedList<MtradeOrder> list = null;
        //System.out.println(qty);
        if (order.getTrade_straregy() != 0 && qty == -1)
            backOrder(order);
        else if (order.getTrade_straregy() != 0 && order.getTrade_straregy() != 3 && order.getTrade_straregy() != 4
                && qty == 0)
            backOrder(order);
        else {
            if (qty == 0) {
                insertOrder(order);
            } else {
                //限价 //对手方最优价格 //本方最优价格
                if (order.getTrade_straregy() == 0 || order.getTrade_straregy() == 3 || order.getTrade_straregy() == 4) {
                    doBdding(order, qty, matchTree.entrySet().iterator());
                    if (qty < order.getOrder_amount())
                        insertOrder(order);
                }
                //最优五档即时成交 剩余撤销 //即时成交剩余撤单
                else if (order.getTrade_straregy() == 1 || order.getTrade_straregy() == 5
                        || order.getTrade_straregy() == 6) {
                    doBdding(order, qty, matchTree.entrySet().iterator());
                    if (qty < order.getOrder_amount())
                        backOrder(order);
                }
                //最优五档即时成交 剩余转限价
                else if (order.getTrade_straregy() == 2) {
                    doBdding(order, qty, matchTree.entrySet().iterator());
                    if (qty < order.getOrder_amount()) {
                        order.setOrder_price(stock.getNew_price());
                        insertOrder(order);
                    }
                }
                //全额成交或撤销委托
                else if (order.getTrade_straregy() == 7) {
                    if (qty < order.getOrder_amount())
                        backOrder(order);
                    else
                        doBdding(order, qty, matchTree.entrySet().iterator());
                } else
                    return false;
            }
        }
        return true;
    }
}