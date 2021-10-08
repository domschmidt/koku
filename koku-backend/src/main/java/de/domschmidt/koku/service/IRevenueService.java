package de.domschmidt.koku.service;

import de.domschmidt.koku.dto.statistic.StatisticsCurrentMonthInfo;

import java.time.LocalDateTime;

public interface IRevenueService {

    StatisticsCurrentMonthInfo generateStatisticsForRange(
            final LocalDateTime start,
            final LocalDateTime end
    );

}
