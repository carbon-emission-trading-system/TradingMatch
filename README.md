# 股票交易撮合系统

### 已实现功能

1、股票的集合竞价

2、股票的连续竞价

​		包括限价委托、沪市的两种市价委托和深市的五种市价委托

3、委托的撤单处理

4、通过redis进行的行情揭露，包括当日成交量、成交额、实时买五卖五等信息。

5、实现了rabbitmq的动态监听，通过定时开关委托队列的监听实现了开市闭市的业务需求。

### 待实现功能

1、日志功能，目前状态信息通过io输出到了控制台，需要将信息记录日志。

2、状态码的编写，目前函数的返回为布尔值，需要定义状态码枚举类，约定返回信息。

### 可能存在的问题

1、异步监听问题

​		撤单和委托的监听异步进行，二者各占一个线程，可能会发生撤单成功但是委托完成撮合的情况。