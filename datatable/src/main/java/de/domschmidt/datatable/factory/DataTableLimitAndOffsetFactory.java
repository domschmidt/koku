package de.domschmidt.datatable.factory;

import de.domschmidt.datatable.dto.query.DataQuerySpecDto;
import de.domschmidt.datatable.factory.exception.DataTableQueryException;

public class DataTableLimitAndOffsetFactory {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_TOTAL = 10;
    public static final int MAX_TOTAL = 1000;
    private static final int MIN_TOTAL = 1;

    private final int limit;
    private final int offset;

    public DataTableLimitAndOffsetFactory(
            final DataQuerySpecDto querySpec
    ) {
        this.limit = calculateLimit(querySpec);
        this.offset = calculateOffset(this.limit, querySpec);
    }

    private int calculateLimit(
            final DataQuerySpecDto querySpec
    ) {
        int total = DEFAULT_TOTAL;
        if (querySpec != null) {
            Integer requestedTotal = querySpec.getTotal();
            if (requestedTotal != null) {
                if (requestedTotal > MAX_TOTAL || requestedTotal < MIN_TOTAL) {
                    throw new DataTableQueryException("Invalid query");
                } else {
                    total = requestedTotal;
                }
            }
        }

        return total;
    }

    private int calculateOffset(
            final long limit,
            final DataQuerySpecDto querySpec
    ) {
        int page = DEFAULT_PAGE;
        if (querySpec != null) {
            Integer requestedPage = querySpec.getPage();
            if (requestedPage != null) {
                if (requestedPage < 0) {
                    throw new DataTableQueryException("Invalid query");
                } else {
                    page = requestedPage;
                }
            }
        }

        return (int) (page * limit);
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }
}
