package ru.hzerr.file;

import java.math.BigDecimal;

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
        switch (this) {
            case BYTE: return size;
            case KB: return multiply(size, 1);
            case MB: return multiply(size, 2);
            case GB: return multiply(size, 3);
            case TB: return multiply(size, 4);
            case PB: return multiply(size, 5);
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal toKb(BigDecimal size) {
        switch (this) {
            case BYTE: return divide(size, 1);
            case KB: return size;
            case MB: return multiply(size, 1);
            case GB: return multiply(size, 2);
            case TB: return multiply(size, 3);
            case PB: return multiply(size, 4);
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal toMb(BigDecimal size) {
        switch (this) {
            case BYTE: return divide(size, 2);
            case KB: return divide(size, 1);
            case MB: return size;
            case GB: return multiply(size, 1);
            case TB: return multiply(size, 2);
            case PB: return multiply(size, 3);
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal toGb(BigDecimal size) {
        switch (this) {
            case BYTE: return divide(size, 3);
            case KB: return divide(size, 2);
            case MB: return divide(size, 1);
            case GB: return size;
            case TB: return multiply(size, 1);
            case PB: return multiply(size, 2);
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal toTb(BigDecimal size) {
        switch (this) {
            case BYTE: return divide(size, 4);
            case KB: return divide(size, 3);
            case MB: return divide(size, 2);
            case GB: return divide(size, 1);
            case TB: return size;
            case PB: return multiply(size, 1);
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal toPb(BigDecimal size) {
        switch (this) {
            case BYTE: return divide(size, 5);
            case KB: return divide(size, 4);
            case MB: return divide(size, 3);
            case GB: return divide(size, 2);
            case TB: return divide(size, 1);
            case PB: return size;
            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
    }

    public BigDecimal to(SizeType type, BigDecimal size) {
        switch (this) {
            case BYTE: switch (type) {
                case BYTE: return size;
                case KB: return divide(size, 1);
                case MB: return divide(size, 2);
                case GB: return divide(size, 3);
                case TB: return divide(size, 4);
                case PB: return divide(size, 5);
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }
            case KB: switch (type) {
                case BYTE: return multiply(size, 1);
                case KB: return size;
                case MB: return divide(size, 1);
                case GB: return divide(size, 2);
                case TB: return divide(size, 3);
                case PB: return divide(size, 4);
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }
            case MB: switch (type) {
                case BYTE: return multiply(size, 2);
                case KB: return multiply(size, 1);
                case MB: return size;
                case GB: return divide(size, 1);
                case TB: return divide(size, 2);
                case PB: return divide(size, 3);
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }
            case GB: switch (type) {
                case BYTE: return multiply(size, 3);
                case KB: return multiply(size, 2);
                case MB: return multiply(size, 1);
                case GB: return size;
                case TB: return divide(size, 1);
                case PB: return divide(size, 2);
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }
            case TB: switch (type) {
                case BYTE: return multiply(size, 4);
                case KB: return multiply(size, 3);
                case MB: return multiply(size, 2);
                case GB: return multiply(size, 1);
                case TB: return size;
                case PB: return divide(size, 1);
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }
            case PB: switch (type) {
                case BYTE: return multiply(size, 5);
                case KB: return multiply(size, 4);
                case MB: return multiply(size, 3);
                case GB: return multiply(size, 2);
                case TB: return multiply(size, 1);
                case PB: return size;
                default: throw new IllegalArgumentException("The " + type.name() + " state cannot be handled");
            }

            default: throw new IllegalStateException("The " + this.name() + " state cannot be handled");
        }
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
