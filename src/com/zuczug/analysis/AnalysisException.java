package com.zuczug.analysis;

import org.ofbiz.base.util.GeneralException;

@SuppressWarnings("serial")
public class AnalysisException extends GeneralException {

    public AnalysisException() {
        super();
    }

    public AnalysisException(Throwable nested) {
        super(nested);
    }

    public AnalysisException(String str) {
        super(str);
    }

    public AnalysisException(String str, Throwable nested) {
        super(str, nested);
    }
}
