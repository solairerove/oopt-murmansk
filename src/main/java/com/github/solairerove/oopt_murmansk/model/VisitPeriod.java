package com.github.solairerove.oopt_murmansk.model;

import java.time.LocalDate;

public record VisitPeriod(LocalDate entryDate, LocalDate exitDate) {

    @Override
    public String toString() {
        return entryDate + " - " + exitDate;
    }
}
