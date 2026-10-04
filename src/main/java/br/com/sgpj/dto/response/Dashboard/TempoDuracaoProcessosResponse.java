package br.com.sgpj.dto.response.Dashboard;

public record TempoDuracaoProcessosResponse(
        Integer ate6Meses,
        Integer de6a12Meses,
        Integer de1a2Anos,
        Integer de2a5Anos,
        Integer mais5Anos
) {}
