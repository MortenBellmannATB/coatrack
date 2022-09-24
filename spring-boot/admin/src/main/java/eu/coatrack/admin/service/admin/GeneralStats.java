package eu.coatrack.admin.service.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GeneralStats {
        public LocalDate dateFrom;
        public LocalDate dateUntil;
        public int callsTotal;
        public int errorsTotal;
        public double revenueTotal;
        public int callsThisPeriod;
        public int errorsThisPeriod;
        public int callsDiff;
        public long users;
    }
