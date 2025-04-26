package com.draccoapp.basisnordestetest.model;

/**
 * Enum que representa os tipos de pessoa no sistema.
 */
public enum PersonType {
    PHYSICAL("Pessoa Física"),
    LEGAL("Pessoa Jurídica");

    private final String displayName;

    PersonType(String displayName) {
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
     * @return O enum correspondente, ou PHYSICAL como padrão.
     */
    public static PersonType fromString(String typeStr) {
        if (typeStr == null) {
            return PHYSICAL;
        }

        try {
            return valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            return PHYSICAL;
        }
    }

    /**
     * Retorna todos os nomes amigáveis em um array para uso em spinners.
     */
    public static String[] getDisplayNames() {
        PersonType[] values = values();
        String[] displayNames = new String[values.length];

        for (int i = 0; i < values.length; i++) {
            displayNames[i] = values[i].getDisplayName();
        }

        return displayNames;
    }

    /**
     * Converte o índice de um spinner para o enum correspondente.
     */
    public static PersonType fromSpinnerPosition(int position) {
        if (position < 0 || position >= values().length) {
            return PHYSICAL;
        }
        return values()[position];
    }

    /**
     * Converte o enum para a posição correspondente no spinner.
     */
    public int toSpinnerPosition() {
        PersonType[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (this == values[i]) {
                return i;
            }
        }
        return 0; // PHYSICAL como padrão
    }
}

