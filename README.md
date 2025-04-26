
# Android Realm App - Sistema de Cadastro de Pessoas

# Desenvolvido por Danilo Ferreira Melo

![Status do Projeto](https://img.shields.io/badge/status-em%20desenvolvimento-brightgreen)
![Versão](https://img.shields.io/badge/versão-1.0.0-blue)
![Licença](https://img.shields.io/badge/licença-MIT-green)

> **Caso de uso: Teste Prático – DEV ANDROID JAVA/KOTLIN**

Um aplicativo Android completo para cadastro e gerenciamento de pessoas físicas e jurídicas, com suporte a múltiplos endereços, geolocalização, armazenamento local com Realm e sincronização com Firebase Cloud Firestore.

## 📋 Índice

- [Recursos e Funcionalidades](#-recursos-e-funcionalidades)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Requisitos do Sistema](#-requisitos-do-sistema)
- [Instalação e Configuração](#-instalação-e-configuração)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Arquitetura](#-arquitetura)
- [Funcionalidades Detalhadas](#-funcionalidades-detalhadas)
- [Integração com Firebase](#-integração-com-firebase)
- [Licença](#-licença)
- [Contato](#-contato)

## 🚀 Recursos e Funcionalidades

- **Cadastro Completo**: Suporte para pessoas físicas (CPF) e jurídicas (CNPJ)
- **Múltiplos Endereços**: Adicione vários endereços para cada pessoa
- **Validação de Dados**: Validação em tempo real de CPF, CNPJ, email e telefone
- **Busca de CEP**: Integração com API ViaCEP para preenchimento automático de endereços
- **Geolocalização**: Captura automática da localização do dispositivo
- **Armazenamento Local**: Persistência de dados com Realm Database
- **Sincronização em Nuvem**: Sincronização com Firebase Cloud Firestore
- **Notificações Push**: Notificações em tempo real com Firebase Cloud Messaging
- **Design Responsivo**: Interface de usuário moderna e adaptável
- **Pesquisa Avançada**: Busca por nome, empresa, CPF ou CNPJ
- **Arquitetura MVVM**: Código organizado seguindo padrões modernos de desenvolvimento

## 💻 Tecnologias Utilizadas

### Principais Frameworks e Bibliotecas

- **Realm Database**: Banco de dados NoSQL para armazenamento local
- **Dagger Hilt**: Injeção de dependência
- **Firebase Cloud Firestore**: Banco de dados em nuvem para sincronização
- **Firebase Cloud Messaging (FCM)**: Sistema de notificações push
- **Google Play Services Location**: Serviços de localização
- **Retrofit**: Cliente HTTP para consumo de APIs
- **Gson**: Conversão entre JSON e objetos Java
- **AndroidX**: Componentes da arquitetura Android moderna
- **Material Design Components**: Componentes de UI seguindo as diretrizes do Material Design

### Dependências Detalhadas

```gradle
// Hilt
implementation 'com.google.dagger:hilt-android:2.44'
kapt 'com.google.dagger:hilt-compiler:2.44'

// Realm
implementation 'io.realm:realm-android-library:10.19.0'
implementation 'io.realm:realm-annotations:10.19.0'

// Retrofit e Gson para busca de CEP
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.google.code.gson:gson:2.10.1'

// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-firestore'
implementation 'com.google.firebase:firebase-messaging'
implementation 'com.google.firebase:firebase-analytics'

// Play Services para localização
implementation 'com.google.android.gms:play-services-location:21.0.1'
