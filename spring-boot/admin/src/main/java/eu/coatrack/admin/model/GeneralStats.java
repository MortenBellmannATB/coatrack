package eu.coatrack.admin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class GeneralStats {
        public LocalDate dateUntil;
        public LocalDate dateFrom;
        public int callsTotal;
        public int errorsTotal;
        public double revenueTotal;
        public int callsThisPeriod;
        public int errorsThisPeriod;
        public int callsDiff;
        public long users;
}
