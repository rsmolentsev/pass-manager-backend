# Автор

Смоленцев Роман Юрьевич, гр. 5130901/10203.

# Password Manager Backend

Backend сервис для менеджера паролей, реализованный на Ktor.

## Требования

- Java 17 или выше
- PostgreSQL 12 или выше
- Gradle 7.6 или выше

## Установка и запуск

### Локальный запуск

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd pass-manager-backend
```

2. Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE passmanager;
```

3. Настройте конфигурацию в файле `src/main/resources/application.conf`:
- Укажите правильные параметры подключения к базе данных
- Измените секретный ключ JWT на свой

4. Соберите и запустите приложение:
```bash
./gradlew build
./gradlew run
```

### Запуск через Docker

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd pass-manager-backend
```

2. Создайте файл `.env` и настройте переменные окружения:
```bash
cp .env.example .env
# Отредактируйте .env файл, указав свои значения
```

3. Запустите приложение:
```bash
docker-compose up -d
```

4. Приложение будет доступно по адресу: `http://localhost:8080`

## API Endpoints

### Аутентификация

- `POST /register` - Регистрация нового пользователя
- `POST /login` - Вход в систему

### Пароли (требуется аутентификация)

- `GET /passwords` - Получение списка всех паролей
- `POST /passwords` - Добавление нового пароля

### Настройки (требуется аутентификация)

- `GET /settings` - Получение настроек пользователя
- `PUT /settings` - Обновление настроек пользователя

## Swagger UI

Документация API доступна по адресу: `http://localhost:8080/swagger`

## Тестирование API

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","masterPassword":"password123"}'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Добавление пароля
```bash
curl -X POST http://localhost:8080/passwords \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"resourceName":"example.com","username":"user@example.com","password":"secret123","notes":"Personal account"}'
```

### Получение списка паролей
```bash
curl -X GET http://localhost:8080/passwords \
  -H "Authorization: Bearer <your-token>"
```

### Обновление настроек
```bash
curl -X PUT http://localhost:8080/settings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"autoLogoutMinutes":60}'
```

## Безопасность

- Все пароли хешируются с использованием BCrypt
- Хранение паролей в базе данных зашифровано с использованием AES
- JWT токены используются для аутентификации
- CORS настроен для безопасного взаимодействия с клиентским приложением
- Секретные данные хранятся в переменных окружения


### Данные

- Данные PostgreSQL сохраняются в именованном томе `postgres_data`
- Конфигурация приложения монтируется из локального файла