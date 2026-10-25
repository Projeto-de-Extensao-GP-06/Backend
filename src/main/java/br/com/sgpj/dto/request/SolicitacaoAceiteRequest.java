package br.com.sgpj.dto.request;

import br.com.sgpj.validation.CpfCnpj;
import br.com.sgpj.validation.CpfCnpjValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

public record SolicitacaoAceiteRequest(
        @NotNull @Valid Client client,
        @NotNull @Valid Process process
) {
    public record Client(
            @NotBlank @Pattern(regexp = "PF|PJ") String type,
            @NotBlank @Size(max = 150) String name,
            @Size(max = 150) String companyName,
            @Email @Size(max = 255) String email,
            @Pattern(regexp = "|\\d{10,11}|\\(?\\d{2}\\)? ?\\d{4,5}-?\\d{4}")
            String phone,
            @CpfCnpj @Size(max = 18) String cpf,
            @Size(max = 15) String rg,
            @CpfCnpj @Size(max = 18) String cnpj,
            @Size(max = 100) String profession,
            @NotNull @Valid Address address,
            @Valid List<Contact> contacts
    ) {
        @AssertTrue(message = "CPF válido é obrigatório para pessoa física")
        public boolean isCpfValidForType() {
            if (!"PF".equals(type)) return true;
            String normalized = digits(cpf);
            return normalized.length() == 11 && CpfCnpjValidator.isValidCpf(normalized);
        }

        @AssertTrue(message = "CNPJ válido e contato principal são obrigatórios para pessoa jurídica")
        public boolean isCompanyDataValidForType() {
            if (!"PJ".equals(type)) return true;
            String normalizedCnpj = cnpj == null ? "" : cnpj.toUpperCase(Locale.ROOT).replaceAll("[.\\-/]", "");
            boolean validCnpj = normalizedCnpj.length() == 14 && CpfCnpjValidator.isValidCnpj(normalizedCnpj);
            boolean validContact = contacts != null && contacts.stream()
                    .anyMatch(contact -> contact != null && !blank(contact.name()) && !blank(contact.profession()));
            return validCnpj && !blank(companyName) && validContact && !blank(profession);
        }
    }

    public record Address(
            @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}") String cep,
            @NotBlank @Size(max = 150) String logradouro,
            @NotBlank @Size(max = 10) String numero,
            @Size(max = 150) String complemento,
            @NotBlank @Size(max = 100) String bairro,
            @NotBlank @Size(max = 100) String cidade,
            @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String estado
    ) {}

    public record Contact(
            @NotBlank @Size(max = 150) String name,
            @Email @Size(max = 255) String email,
            @Pattern(regexp = "|\\d{10,11}|\\(?\\d{2}\\)? ?\\d{4,5}-?\\d{4}") String phone,
            @Size(max = 100) String profession,
            Boolean isPrimary
    ) {}

    public record Process(
            @Size(max = 25) String cnj,
            @NotBlank @Size(max = 20) String priority,
            @NotBlank @Size(max = 60) String phase,
            LocalDate hearingDate,
            LocalTime hearingTime,
            @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 13, fraction = 2) BigDecimal value
    ) {
        @AssertTrue(message = "Data e hora da audiência devem ser informadas juntas")
        public boolean isHearingDateTimeComplete() {
            return (hearingDate == null) == (hearingTime == null);
        }

        @AssertTrue(message = "Número CNJ inválido")
        public boolean isCnjValid() {
            if (cnj == null || cnj.isBlank()) return true;
            String normalized = digits(cnj);
            if (normalized.length() != 20) return false;
            String body = normalized.substring(0, 7) + normalized.substring(9) + "00";
            int checkDigit = 98 - new BigInteger(body).mod(BigInteger.valueOf(97)).intValue();
            return checkDigit == Integer.parseInt(normalized.substring(7, 9));
        }
    }

    private static String digits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}