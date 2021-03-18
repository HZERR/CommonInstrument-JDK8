package ru.hzerr.file;

import java.math.BigDecimal;
import java.math.BigInteger;

@SuppressWarnings("DuplicatedCode")
public enum SizeType {
    BYTE,
    KB,
    MB,
    GB,
    TB,
    PB;

    private static final BigDecimal BINARY_THOUSAND = BigDecimal.valueOf(1024);

    public BigDecimal toByte(BigDecimal size) {
        return switch (this) {
            case BYTE -> size;
            case KB -> multiply(size, 1);
            case MB -> multiply(size, 2);
            case GB -> multiply(size, 3);
            case TB -> multiply(size, 4);
            case PB -> multiply(size, 5);
        };
    }

    public BigDecimal toKb(BigDecimal size) {
        return switch (this) {
            case BYTE -> divide(size, 1);
            case KB -> size;
            case MB -> multiply(size, 1);
            case GB -> multiply(size, 2);
            case TB -> multiply(size, 3);
            case PB -> multiply(size, 4);
        };
    }

    public BigDecimal toMb(BigDecimal size) {
        return switch (this) {
            case BYTE -> divide(size, 2);
            case KB -> divide(size, 1);
            case MB -> size;
            case GB -> multiply(size, 1);
            case TB -> multiply(size, 2);
            case PB -> multiply(size, 3);
        };
    }

    public BigDecimal toGb(BigDecimal size) {
        return switch (this) {
            case BYTE -> divide(size, 3);
            case KB -> divide(size, 2);
            case MB -> divide(size, 1);
            case GB -> size;
            case TB -> multiply(size, 1);
            case PB -> multiply(size, 2);
        };
    }

    public BigDecimal toTb(BigDecimal size) {
        return switch (this) {
            case BYTE -> divide(size, 4);
            case KB -> divide(size, 3);
            case MB -> divide(size, 2);
            case GB -> divide(size, 1);
            case TB -> size;
            case PB -> multiply(size, 1);
        };
    }

    public BigDecimal toPb(BigDecimal size) {
        return switch (this) {
            case BYTE -> divide(size, 5);
            case KB -> divide(size, 4);
            case MB -> divide(size, 3);
            case GB -> divide(size, 2);
            case TB -> divide(size, 1);
            case PB -> size;
        };
    }

    @SuppressWarnings("BigDecimalMethodWithoutRoundingCalled")
    public BigDecimal to(SizeType type, BigDecimal size) {
        return switch (this) {
            case BYTE -> switch (type) {
                case BYTE -> size;
                case KB -> divide(size, 1);
                case MB -> divide(size, 2);
                case GB -> divide(size, 3);
                case TB -> divide(size, 4);
                case PB -> divide(size, 5);
            };
            case KB -> switch (type) {
                case BYTE -> multiply(size, 1);
                case KB -> size;
                case MB -> divide(size, 1);
                case GB -> divide(size, 2);
                case TB -> divide(size, 3);
                case PB -> divide(size, 4);
            };
            case MB -> switch (type) {
                case BYTE -> multiply(size, 2);
                case KB -> multiply(size, 1);
                case MB -> size;
                case GB -> divide(size, 1);
                case TB -> divide(size, 2);
                case PB -> divide(size, 3);
            };
            case GB -> switch (type) {
                case BYTE -> multiply(size, 3);
                case KB -> multiply(size, 2);
                case MB -> multiply(size, 1);
                case GB -> size;
                case TB -> divide(size, 1);
                case PB -> divide(size, 2);
            };
            case TB -> switch (type) {
                case BYTE -> multiply(size, 4);
                case KB -> multiply(size, 3);
                case MB -> multiply(size, 2);
                case GB -> multiply(size, 1);
                case TB -> size;
                case PB -> divide(size, 1);
            };
            case PB -> switch (type) {
                case BYTE -> multiply(size, 5);
                case KB -> multiply(size, 4);
                case MB -> multiply(size, 3);
                case GB -> multiply(size, 2);
                case TB -> multiply(size, 1);
                case PB -> size;
            };
        };
    }

    private BigDecimal multiply(BigDecimal decimal, int count) {
        for (int i = 0; i < count; i++) {
            decimal = decimal.multiply(BINARY_THOUSAND);
        }

        return decimal;
    }

    private BigDecimal divide(BigDecimal decimal, int count) {
        for (int i = 0; i < count; i++) {
            decimal = decimal.divide(BINARY_THOUSAND);
        }

        return decimal;
    }
}
