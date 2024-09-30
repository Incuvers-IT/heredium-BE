package art.heredium.excel.manager;

import java.util.Arrays;

public enum ExcelFileName {
    EMPTY(-1, "파일 이름 없음"),
    MODEL(0, "농기계 모델 목록"),
    ;

    public int getType() {
        return type;
    }

    public String getFileName() {
        return this.fileName;
    }

    private int type;
    private String fileName;

    ExcelFileName(int type, String fileName) {
        this.type = type;
        this.fileName = fileName;
    }

    public static ExcelFileName getExcelFileName(int type) {
        return Arrays
                .stream(values())
                .filter(method -> method.getType() == type)
                .findFirst()
                .orElse(ExcelFileName.EMPTY);
    }
}
