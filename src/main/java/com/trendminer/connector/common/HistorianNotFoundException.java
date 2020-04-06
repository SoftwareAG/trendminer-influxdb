/**
 * @created ${YEAR}
 * @project ${PROJECT_NAME}
 * @author ${USER}
 * @copyright Software AG
 */

package com.trendminer.connector.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HistorianNotFoundException extends RuntimeException {
}
