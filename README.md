# Weather App

Приложение для отображения погоды с поддержкой оффлайн режима и виджетом для домашнего экрана. Данные берутся из Open-Meteo API.
Данное приложение получает координаты пользователя (широту, долготу) и по ней определяется текущий город пользователя, относительно которого и строится прогноз погоды, благодаря данным с https://api.open-meteo.com/

## Функциональность

- Текущая погода и прогноз на неделю вперед.
- Кэширование данных и работа без интернета при помощи Room
- Смена дневного и ночного фона в зависимости от времени суток
- Анимации погодных состояний через Lottie
- График изменения температуры (Vico)
- Виджет на домашний экран (Glance)
- Навигация между основными экранами

## Стек

- Kotlin
- Jetpack Compose
- Navigation Compose
- Retrofit + Gson converter
- Room
- Coroutines
- ViewModel / Lifecycle
- Glance App Widget
- Vico (графики)
- Lottie

## Скриншоты
Главный экран (дневной режим):


<img width="405" height="903" alt="image" src="https://github.com/user-attachments/assets/a0c32bc3-3c97-456a-b486-c366de0c2dce" />


Ночной режим:

<img width="407" height="901" alt="image" src="https://github.com/user-attachments/assets/e25cca98-712d-4518-8be5-14af493fa6f5" />


График температуры для выбранного дня (можно листать слева-направо): 



<img width="401" height="900" alt="image" src="https://github.com/user-attachments/assets/3813df7b-8e4b-4cf0-b757-a1fa355ffb31" />


<img width="404" height="903" alt="image" src="https://github.com/user-attachments/assets/664a5e34-5639-4429-8856-4c0b8d7fbf9d" />



Домашний виджет:


<img width="405" height="904" alt="image" src="https://github.com/user-attachments/assets/797d6e85-dae9-4b97-90ac-6f2200f7105d" />



## Запуск

1. Клонировать репозиторий
2. Открыть проект в Android Studio
3. Дождаться синхронизации Gradle
4. Запустить приложение на устройстве или эмуляторе
