# VELORA Android — первая версия

Проект создан на основе учебного SavdoShopApp и адаптирован под магазин VELORA.

## Архитектура

Android (Kotlin + Jetpack Compose) → HTTPS REST API → существующий Node.js/Express на Render → Supabase PostgreSQL + MongoDB Atlas.

Приложение не подключается к PostgreSQL/MongoDB напрямую и не содержит ключей баз данных. Используется тот же backend, что и у сайта:

`https://velora-bigdata-project.onrender.com`

## Что уже подключено

- загрузка категорий из `GET /api/categories`;
- каталог с поиском и фильтрацией через `GET /api/products`;
- карточка товара, варианты размера/цвета и отзывы через `GET /api/products/:id`;
- добавление в корзину и изменение количества (пока локально в памяти приложения);
- оформление заказа через `POST /api/orders` с несколькими позициями в одной транзакции;
- визуальная основа в стиле сайта VELORA: светлый фон, крупный fashion-баннер, типографика с засечками, минималистичные карточки.

## Открытие в Android Studio

1. Распакуйте архив проекта.
2. В Android Studio выберите **Open** и укажите папку `SavdoShopApp`.
3. Дождитесь Gradle Sync. При первом запуске Gradle может скачать необходимые зависимости.
4. Запустите приложение на эмуляторе или Android-телефоне.

## Важно

- Корзина пока хранится в памяти и очищается после перезапуска приложения.
- Регистрация/авторизация пока не подключена: в текущем backend сайта нет соответствующих API-маршрутов.
- История заказов и Dashboard будут добавлены следующим этапом.
- Для выпуска APK используйте **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

## Проверка backend

Откройте в браузере:

- `https://velora-bigdata-project.onrender.com/api/health`
- `https://velora-bigdata-project.onrender.com/api/categories`
- `https://velora-bigdata-project.onrender.com/api/products`

Если Render уснул, первый запрос может выполняться дольше обычного.
