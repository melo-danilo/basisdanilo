package com.draccoapp.basisnordestetest.model;

/**
 * Enum que representa os tipos de endereço no sistema.
 */
public enum AddressType {
    RESIDENTIAL("Residencial"),
    COMMERCIAL("Comercial");

    private final String displayName;

    AddressType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retorna o nome amigável para exibição.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Converte uma string para o enum correspondente.
     * @param typeStr A string a ser convertida.
     * @return O enum correspondente, ou RESIDENTIAL como padrão.
     */
    public static AddressType fromString(String typeStr) {
        if (typeStr == null) {
            return RESIDENTIAL;
        }

        try {
            return valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return RESIDENTIAL;
        }
    }

    /**
     * Retorna todos os nomes amigáveis em um array para uso em spinners.
     */
    public static String[] getDisplayNames() {
        AddressType[] values = values();
        String[] displayNames = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            displayNames[i] = values[i].getDisplayName();
        }

        return displayNames;
    }

    /**
     * Converte o índice de um spinner para o enum correspondente.
     */
    public static AddressType fromSpinnerPosition(int position) {
        if (position < 0 || position >= values().length) {
            return RESIDENTIAL;
        }
        return values()[position];
    }

    /**
     * Converte o enum para a posição correspondente no spinner.
     */
    public int toSpinnerPosition() {
        AddressType[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (this == values[i]) {
                return i;
            }
        }
        return 0; // RESIDENTIAL como padrão
    }
}
