# Privacy Policy

_Last updated: October 18, 2026_

**English version below · [Versão em português mais abaixo](#política-de-privacidade)**

"Allowed Names in Portugal" ("the App") is developed and maintained by Pedro Vicente
([github.com/neteinstein](https://github.com/neteinstein)). This
Privacy Policy covers both the Android app and the website version of the App
([nomespermitidosemportugal.neteinstein.org](https://nomespermitidosemportugal.neteinstein.org/)),
and explains what information the App accesses and how it is used. Where the two differ, this
policy says so.

## Summary

- The App does **not** collect, store, or transmit any personal data.
- The App does **not** use analytics, advertising, or crash-reporting SDKs.
- The App has **no user accounts** and does not require sign-in.
- All app data (the names list and your settings) is stored **locally on your device** (or, on
  the website, in your browser).
- The website sets **no cookies**.

## What the App does

The App downloads the official "Lista de Nomes Próprios" PDF published by Portugal's
[Instituto dos Registos e do Notariado](https://irn.justica.gov.pt/en-gb/) (IRN), extracts the
list of legally allowed first names, and stores it in a local database on your device so you
can browse, search, and filter it.

The website shows the same list. Instead of downloading the PDF itself, it loads a copy of the
list that is extracted from the same IRN PDF and published alongside the website.

## Permissions

The Android app requests:

| Permission | Why it's needed |
|---|---|
| `INTERNET` | To download the names list PDF from the configured source (the IRN's published PDF by default) and to check for updates. |

The App does not request access to your contacts, location, camera, microphone, photos, or any
other sensitive device permission.

The website does not ask your browser for any permission (such as location, camera,
microphone, or notifications).

## Information we collect

We (the developer) do not collect, receive, or have access to any information about you or your
use of the App. Specifically:

- **No account or personal information** is requested or stored.
- **No analytics, telemetry, or crash-reporting** libraries are included in the App.
- **No advertising SDKs** are included in the App.
- **No data is shared with or sold to third parties**, because none is collected in the first
  place.

## Data stored on your device

The Android app stores the following locally, using Android's Room database and DataStore
Preferences. This data stays on your device and is never sent to us:

- The parsed list of allowed names, downloaded from the configured source.
- Your app preferences (e.g. the configured names-source URL, automatic refresh frequency,
  language).

This local data is deleted automatically when you uninstall the App. If you use Android's
built-in "Back up my data" feature, this local app data may be included in your device's backup
to your own Google Account, per standard Android OS behavior — we have no access to it.

The website stores the following in your browser's local storage (`localStorage`). It stays in
your browser and is never sent to us:

- The list of allowed names and when it was last loaded.
- The language you picked in Settings, if any.
- Whether you dismissed the "Get the Android app" banner.

You can delete this data at any time by clearing this site's data in your browser settings.

## Network requests

When the App downloads or refreshes the names list, it makes a direct HTTP(S) request to the
source URL configured in Settings (the IRN's published PDF, by default). We do not attach any
personal identifiers to this request. If you choose to change the source URL to a different
server, that server's own privacy practices and logs (e.g. standard web server access logs,
which may include your IP address) are outside of our control — only point the App at a source
you trust.

The website is hosted on [GitHub Pages](https://pages.github.com/). When you visit it, your
browser requests the website's files (including the names list) from GitHub's servers. As the
hosting provider, GitHub may log technical information such as your IP address, as described in
the [GitHub Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).
We do not have access to those logs.

## Name-meaning search

If you choose to look up the meaning of a name, the App opens a web search for it: on Android,
a [Brave Search](https://search.brave.com/) page (shown inside the App); on the website, a
Google Search page in a new browser tab. The search contains only a question about that name (for
example, "What's the meaning of the name Maria"). That search engine's own privacy policy applies to that search.

## Children's privacy

The App does not knowingly collect personal data from anyone, including children, because it
does not collect personal data at all.

## Changes to this policy

We may update this Privacy Policy if the App's functionality changes (for example, if a future
version adds analytics or features that require data collection). Any changes will be published
in this file, with an updated "Last updated" date at the top.

## Contact us

Questions about this Privacy Policy? Send an email to play-store-neteinstein@googlegroups.com

---

# Política de Privacidade

_Última atualização: 18 de outubro de 2026_

**[English version above](#privacy-policy) · Versão em português abaixo**

A aplicação "Nomes Permitidos em Portugal" ("a App") é desenvolvida e mantida por Pedro Vicente
([github.com/neteinstein](https://github.com/neteinstein)) como um projeto de código aberto.
Esta Política de Privacidade abrange tanto a aplicação Android como a versão web da App
([nomespermitidosemportugal.neteinstein.org](https://nomespermitidosemportugal.neteinstein.org/)),
e explica que informação a App acede e como é utilizada. Quando as duas diferem, esta política
indica-o.

## Resumo

- A App **não** recolhe, armazena ou transmite quaisquer dados pessoais.
- A App **não** utiliza bibliotecas de analytics, publicidade ou relatório de falhas (crash
  reporting).
- A App **não tem contas de utilizador** nem requer autenticação.
- Todos os dados da App (a lista de nomes e as suas definições) são guardados **apenas no seu
  dispositivo** (ou, no site, no seu navegador).
- O site **não utiliza cookies**.

## O que a App faz

A App descarrega o PDF oficial "Lista de Nomes Próprios" publicado pelo
[Instituto dos Registos e do Notariado](https://irn.justica.gov.pt/pt-pt/) (IRN), extrai a lista
de nomes próprios legalmente permitidos e guarda-a numa base de dados local no seu dispositivo,
para que possa consultar, procurar e filtrar essa lista.

O site mostra a mesma lista. Em vez de descarregar o próprio PDF, carrega uma cópia da lista
extraída do mesmo PDF do IRN e publicada juntamente com o site.

## Permissões

A aplicação Android solicita:

| Permissão | Porque é necessária |
|---|---|
| `INTERNET` | Para descarregar o PDF da lista de nomes a partir da fonte configurada (por predefinição, o PDF publicado pelo IRN) e para verificar atualizações. |

A App não solicita acesso aos seus contactos, localização, câmara, microfone, fotografias, ou
qualquer outra permissão sensível do dispositivo.

O site não pede ao seu navegador qualquer permissão (como localização, câmara, microfone ou
notificações).

## Informação que recolhemos

Nós (o desenvolvedor) não recolhemos, recebemos, nem temos acesso a qualquer informação sobre si
ou sobre a sua utilização da App. Concretamente:

- **Não é pedida nem guardada** qualquer conta ou informação pessoal.
- **Não estão incluídas** bibliotecas de analytics, telemetria ou relatório de falhas.
- **Não estão incluídos** SDKs de publicidade.
- **Nenhum dado é partilhado ou vendido a terceiros**, uma vez que nenhum dado é recolhido.

## Dados guardados no seu dispositivo

A aplicação Android guarda localmente os seguintes dados, através da base de dados Room e das
DataStore Preferences do Android. Estes dados permanecem no seu dispositivo e nunca nos são enviados:

- A lista de nomes permitidos, descarregada a partir da fonte configurada.
- As suas preferências (por exemplo, o URL da fonte de nomes configurado, a frequência de
  atualização automática, o idioma).

Estes dados locais são eliminados automaticamente quando desinstala a App. Se utilizar a
funcionalidade nativa do Android "Cópia de segurança dos meus dados", estes dados locais da App
podem ser incluídos na cópia de segurança do seu dispositivo para a sua própria Conta Google,
seguindo o comportamento padrão do sistema Android — não temos qualquer acesso a essa cópia.

O site guarda os seguintes dados no armazenamento local do seu navegador (`localStorage`).
Estes dados permanecem no seu navegador e nunca nos são enviados:

- A lista de nomes permitidos e a data em que foi carregada pela última vez.
- O idioma que escolheu nas Definições, se for o caso.
- Se fechou o aviso "Instale a aplicação Android".

Pode eliminar estes dados a qualquer momento, limpando os dados deste site nas definições do
seu navegador.

## Pedidos de rede

Quando a App descarrega ou atualiza a lista de nomes, efetua um pedido HTTP(S) direto ao URL da
fonte configurado nas Definições (por predefinição, o PDF publicado pelo IRN). Não associamos
qualquer identificador pessoal a este pedido. Se optar por alterar o URL da fonte para outro
servidor, as práticas de privacidade e os registos desse servidor (por exemplo, registos padrão
de acesso a servidores web, que podem incluir o seu endereço IP) estão fora do nosso controlo —
utilize apenas uma fonte em que confie.

O site está alojado no [GitHub Pages](https://pages.github.com/). Quando o visita, o seu
navegador pede os ficheiros do site (incluindo a lista de nomes) aos servidores do GitHub.
Enquanto fornecedor de alojamento, o GitHub pode registar informação técnica, como o seu
endereço IP, conforme descrito na
[Declaração de Privacidade do GitHub](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).
Não temos acesso a esses registos.

## Pesquisa do significado de nomes

Se optar por pesquisar o significado de um nome, a App abre uma pesquisa na web: no Android,
uma página do [Brave Search](https://search.brave.com/) (mostrada dentro da App); no site, uma
página de pesquisa do Google num novo separador do navegador. A pesquisa contém apenas uma pergunta sobre esse
nome (por exemplo, "Qual é o significado do nome Maria"). Aplica-se a essa pesquisa a política de privacidade do
respetivo motor de pesquisa.

## Privacidade de menores

A App não recolhe, com conhecimento, dados pessoais de ninguém, incluindo crianças, uma vez que
não recolhe dados pessoais de forma alguma.

## Alterações a esta política

Podemos atualizar esta Política de Privacidade caso a funcionalidade da App seja alterada (por
exemplo, se uma versão futura adicionar analytics ou funcionalidades que exijam recolha de
dados). Quaisquer alterações serão publicadas neste ficheiro, com a data de "Última atualização"
no topo atualizada.

## Contacte-nos

Tem questões sobre esta Política de Privacidade? Envie um email para play-store-neteinstein@googlegroups.com
