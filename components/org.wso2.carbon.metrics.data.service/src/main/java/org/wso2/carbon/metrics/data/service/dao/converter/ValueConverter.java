package org.wso2.carbon.metrics.data.service.dao.converter;

import java.math.BigDecimal;

/**
 * Convert value
 */
public interface ValueConverter {

    BigDecimal convert(BigDecimal value);

}
