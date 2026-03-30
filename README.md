# NetChat

Консольный **peer-to-peer** чат с прямыми соединениями (без центрального сервера). На старте указывается имя пользователя и, опционально, адрес peer-а для подключения; если адрес не указан - приложение работает в режиме ожидания входящих подключений.

## Документация

| Документ | Содержание |
|----------|------------|
| [doc/architecture.md](doc/architecture.md) | Требования + архитектура + диаграммы + обоснование технологий + распределение задач |
| [doc/testing.md](doc/testing.md) | План тестирования |

## Технологии

- **Язык:** Java 21 (LTS)
- **Сборка:** Maven
- **Транспорт:** TCP (прямые соединения)
- **Протокол сообщений:** NDJSON (JSON-объекты, разделённые `\n`), UTF-8
- **Тесты:** JUnit 5
- **CI:** GitHub Actions (сборка, тесты, проверка форматирования)

## Структура репозитория (целевая)

```
.
├── .github/workflows/       # CI
├── doc/                     # требования, архитектура, тест-план, PlantUML
├── src/
│   ├── main/java/           # исходники приложения
│   └── test/java/           # тесты
├── pom.xml
├── README.md
├── LICENSE
└── .gitignore
```

## Сборка и запуск (после реализации)

Зависимости:

- JDK 21+
- Maven 3.9+

Сборка:

```bash
mvn -B -ntp verify
```

Запуск (fat jar собирается в `target/netchat.jar`):

```bash
java -jar target/netchat.jar --help
```

Примеры целевого CLI:

- **Ожидание подключения (server mode):** `java -jar target/netchat.jar --name Alice --listen 0.0.0.0:9000`
- **Подключение к peer (client mode):** `java -jar target/netchat.jar --name Bob --peer 192.168.1.5:9000`

Выход: команда `/exit` или EOF в stdin.

## Лицензия

См. файл [LICENSE](LICENSE).
