# Robot Simulator

### 🚀 Описание проекта
Проект — это **визуальный симулятор робота**, который:
- Отображает робота на экране.
- Позволяет устанавливать целевую точку кликом мыши.
- Автоматически перемещает робота к цели с реалистичной физикой движения и ограничениями по скорости и угловой скорости.
- Поддерживает плавную анимацию и корректную работу при закрытии приложения.

Проект основан на классах `RobotModel`, `GameVisualizer`, `MainApplicationFrame`, `RobotCoordinatesWindow` и др.

---

### 🛠 Основные компоненты проекта

- **RobotModel**  
  Модель данных робота: хранит координаты `(x, y)`, направление движения, целевую точку, обновляет своё состояние каждую 0.01 секунды.
  
- **GameVisualizer**  
  Отвечает за отрисовку робота и цели на экране. Обновляется по таймеру.

- **MainApplicationFrame**  
  Главное окно программы, содержащее:
  - окно с координатами робота
  - окно логов
  - окно визуализации
  - меню приложения

- **RobotCoordinatesWindow**  
  Отдельное окно, отображающее текущие координаты и направление робота в реальном времени.

---

### 🧠 Особенности логики движения

- Обновление позиции происходит каждые **10 мс** (`0.01` сек).
- Робот движется с ограниченной максимальной скоростью:
  - Линейная скорость (`MAX_VELOCITY`) — **0.3** (увеличено с 0.1 для большей скорости).
  - Угловая скорость (`MAX_ANGULAR_VELOCITY`) — **0.01** (увеличено с 0.001 для более быстрого поворота).
- При достижении цели на расстоянии менее `0.5` и с минимальным угловым отклонением менее `0.05` радиан, робот останавливается.
- При необходимости робот сначала корректирует направление, затем двигается прямо к цели.

---

### 🖱 Управление

- **Клик мышью по полю визуализации** — задать новую цель для перемещения робота.

---
