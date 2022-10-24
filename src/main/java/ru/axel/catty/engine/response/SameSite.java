package ru.axel.catty.engine.response;

public enum SameSite implements ISameSite {
    NONE("None"), // Атрибут Secure также должен быть установлен при установке этого значения, например SameSite=None; Secure
    STRICT("Strict"),
    LAX("Lax"); // по умолчанию

    private final String value;

    SameSite(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}