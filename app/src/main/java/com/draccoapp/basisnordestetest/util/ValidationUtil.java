package com.draccoapp.basisnordestetest.util;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtil {

    /**
     * Valida um CPF.
     * @param cpf CPF com ou sem formatação
     * @return true se o CPF for válido
     */
    public static boolean isValidCpf(String cpf) {
        if (TextUtils.isEmpty(cpf)) {
            return false;
        }

        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        // CPF deve ter 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (caso inválido)
        boolean allDigitsEqual = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allDigitsEqual = false;
                break;
            }
        }
        if (allDigitsEqual) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int remainder = sum % 11;
        int digit1 = remainder < 2 ? 0 : 11 - remainder;

        // Verifica o primeiro dígito verificador
        if (digit1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        // Calcula o segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        remainder = sum % 11;
        int digit2 = remainder < 2 ? 0 : 11 - remainder;

        // Verifica o segundo dígito verificador
        return digit2 == (cpf.charAt(10) - '0');
    }

    /**
     * Valida um CNPJ.
     * @param cnpj CNPJ com ou sem formatação
     * @return true se o CNPJ for válido
     */
    public static boolean isValidCnpj(String cnpj) {
        if (TextUtils.isEmpty(cnpj)) {
            return false;
        }

        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("[^0-9]", "");

        // CNPJ deve ter 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (caso inválido)
        boolean allDigitsEqual = true;
        for (int i = 1; i < cnpj.length(); i++) {
            if (cnpj.charAt(i) != cnpj.charAt(0)) {
                allDigitsEqual = false;
                break;
            }
        }
        if (allDigitsEqual) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int[] multipliers1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * multipliers1[i];
        }
        int remainder = sum % 11;
        int digit1 = remainder < 2 ? 0 : 11 - remainder;

        // Verifica o primeiro dígito verificador
        if (digit1 != (cnpj.charAt(12) - '0')) {
            return false;
        }

        // Calcula o segundo dígito verificador
        int[] multipliers2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * multipliers2[i];
        }
        remainder = sum % 11;
        int digit2 = remainder < 2 ? 0 : 11 - remainder;

        // Verifica o segundo dígito verificador
        return digit2 == (cnpj.charAt(13) - '0');
    }

    /**
     * Valida um número de telefone.
     * @param phone Telefone com ou sem formatação
     * @return true se o telefone for válido
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }

        // Remove caracteres não numéricos
        phone = phone.replaceAll("[^0-9]", "");

        // Telefone deve ter 10 ou 11 dígitos (com DDD)
        return phone.length() == 10 || phone.length() == 11;
    }

    /**
     * Valida um endereço de email.
     * @param email Endereço de email
     * @return true se o email for válido
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
