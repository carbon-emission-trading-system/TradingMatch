package com.xMarket.matching.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xMarket.matching.Model.TradedInst;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<TradedInst, Integer> {

}
