package br.com.sgpj.dto.response.Dashboard;

public record EvolucaoCarteiraResponse(

        int ativos,
        int encerrados,
        int suspensos,
        int novos

){}
