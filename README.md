# Система бронирования помещений

Простое веб-приложение для бронирования переговорных и других помещений.  
Проект состоит из одного Maven-модуля с двумя серверами:  
Backend (Spring Boot, Java 21, MySQL) и Frontend (React + TypeScript + Vite).

## Технологии

- Java: 21  
- Backend: Spring Boot, Spring Data JPA, MySQL  
- Frontend: React, TypeScript, Vite  
- Сборка: Maven и npm  

## Запуск проекта локально

### 1. Клонирование репозитория

```bash
git clone https://github.com/<ваш_никнейм>/room-booking-system.git
cd room-booking-system
```

### 2. Настройка базы данных

Перед запуском убедитесь, что установлен MySQL и создана база данных, например `booking_db`.

В файле `src/main/resources/application.properties` укажите параметры подключения:

```
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 3. Запуск backend

```bash
mvn spring-boot:run
```

После запуска сервер будет доступен по адресу:  
http://localhost:8080

### 4. Настройка frontend

Перейдите в папку `booking-ui/` и установите зависимости:

```bash
cd frontend
npm install
```

Файл `.env` в этой папке должен содержать:
```
VITE_API_BASE_URL=/
```

Это значение корректно работает в режиме разработки благодаря proxy,  
указанному в `vite.config.ts`.

### 5. Запуск frontend

Всё ещё находясь в папке `booking-ui`:

```bash
npm run dev
```

После этого фронтенд будет доступен по адресу:  
http://localhost:5173  
(а API — через прокси на http://localhost:8080/api/...)

## Авторизация пользователя

Перед бронированием необходимо указать имя пользователя (в верхней части интерфейса).  
Имя сохраняется в браузере и передаётся в заголовке `X-User` при каждом запросе.

## Хранение данных

Данные сохраняются в базе MySQL.  
При первом запуске таблицы создаются автоматически  
(если включено `spring.jpa.hibernate.ddl-auto=update`).

## Сборка production-версии фронтенда

Если вы хотите встроить фронтенд в Spring Boot:

```bash
cd booking-ui
npm run build
```

После этого собранные файлы (`booking-ui/dist`) можно поместить в  
`src/main/resources/static/` — тогда приложение будет доступно по адресу:  
http://localhost:8080

## Автор

Проект разработан как учебный пример для демонстрации базовой архитектуры:  
Spring Boot + React + REST API + сохранение состояния между перезапусками.
