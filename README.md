# 📱 PresenteApp

O **PresenteApp** é uma solução Android nativa desenvolvida para modernizar e automatizar o controlo de presença em ambientes académicos. Através da leitura de **QR Codes dinâmicos** e integração com serviços de backend, o sistema elimina a necessidade de listas de papel, reduzindo fraudes e otimizando o tempo de professores e alunos.

---

## 🎯 Objetivo e Foco

O projeto foi concebido para oferecer uma ferramenta de gestão de presença segura e em tempo real. 
- **Foco:** Autenticidade e facilidade de uso.
- **Público-alvo:** Instituições de ensino, professores e estudantes.

---

## ✨ Funcionalidades

### 🔐 Níveis de Acesso
O sistema possui três tipos de perfis, cada um com responsabilidades específicas:

1.  **Administrador (Admin Master):**
    * Gestão de utilizadores de alto nível.
    * Aprovação e monitorização de novos professores no sistema.
    * Controlo de professores ativos e pendentes.

2.  **Professor:**
    * Registo e aguardo de aprovação administrativa.
    * Geração de **QR Codes** exclusivos para cada sessão de aula.
    * Gestão de alunos: Aprovação de novos alunos que solicitam entrada na disciplina.
    * Visualização de dashboards de controlo.

3.  **Aluno:**
    * Registo vinculado a um curso e RA (Registo Académico).
    * Scanner de QR Code integrado para marcação de presença instantânea.
    * Interface intuitiva para acompanhamento de status.

---

## 🛠️ Tecnologias Utilizadas

Este ecossistema utiliza tecnologias modernas para garantir performance e escalabilidade:

* **Linguagem:** [Kotlin](https://kotlinlang.org/) (Android Nativo).
* **Arquitetura:** MVVM (Model-View-ViewModel) para separação de lógica e UI.
* **Interface:** Material Design 3 com ViewBinding.
* **Comunicação API:** [Retrofit](https://square.github.io/retrofit/) & OkHttp.
* **Serviços Cloud:** Firebase (Auth para autenticação e Google Services).
* **Scanner:** ML Kit / ZXing para processamento de QR Codes.
* **Processamento Assíncrono:** Kotlin Coroutines.

---

## 🚀 Como Instalar e Configurar

### Pré-requisitos
* Android Studio Jellyfish ou superior.
* JDK 17 ou 21.
* Dispositivo Android ou Emulador (API 24+).

### Passos para Configuração

1. **Clonar o Repositório:**
   ```bash
   git clone [https://github.com/robitoos/app-presenca.git](https://github.com/robitoos/app-presenca.git)
