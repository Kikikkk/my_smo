package com.example;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class StepModeController {

    @FXML
    public Scene stepModePage;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public AnchorPane diagramPane;
    @FXML
    public Button finishButton;
    @FXML
    public Button nextButton;

    private List<CustomState> states;
    private int lastStateId = -1;
    private static final int LINE_DISTANCE = 50;
    private static final int LETTER_POS = 12;
    private static final int TACT_HEIGHT = (int) (LINE_DISTANCE * 0.8);
    private static final int TIME_TO_DISTANCE_FACTOR = 60;
    private static final int WIDTH = 900;
    private static final Color COLOR_NORMAL = Color.BLACK;
    private static final Color COLOR_FAILED = Color.RED;
    private static final Color COLOR_EVENT = Color.GREEN;
    private static final Color COLOR_BUFFER = Color.BLUE;
    private static final Color COLOR_DEVICE = Color.ORANGE;

    private double prevTime;
    private double rightTime;

    public void sendStatistic(Statistic statistic) {
        this.states = new ArrayList<>(statistic.fillConditions());
        signDiagram();
        nextButtonClick();
    }

    private void signDiagram() {
        int sourceCount = CustomState.getSourceCount();
        int bufferCapacity = CustomState.getBufferCapacity();
        int deviceCount = CustomState.getDeviceCount();

        for (int i = 1; i <= sourceCount; i++) {
            sign(0, (i - 1) * LINE_DISTANCE + LETTER_POS, "И" + i);
        }
        for (int i = 1; i <= deviceCount; i++) {
            sign(0, (i - 1 + sourceCount) * LINE_DISTANCE + LETTER_POS, "П" + i);
        }
        for (int i = 1; i <= bufferCapacity; i++) {
            sign(0, (i - 1 + sourceCount + deviceCount) * LINE_DISTANCE + LETTER_POS, "Б" + i);
        }
        sign(0, (sourceCount + deviceCount + bufferCapacity) * LINE_DISTANCE + LETTER_POS, "ОТК");
    }

    @FXML
    public void nextButtonClick() {
        if (lastStateId == states.size() - 1) {
            nextButton.setDisable(true);
            finishButton.setDisable(true);
            return;
        }

        CustomState lastState = states.get(++lastStateId);
        prevTime = rightTime;
        rightTime = lastState.getTime();
        drawLines();
        drawCondition(lastState);
        scrollPane.setHvalue(1);
    }

    private void drawLines() {
        int sourceCount = CustomState.getSourceCount();
        int bufferSize = CustomState.getBufferCapacity();
        int deviceCount = CustomState.getDeviceCount();

        int linesCount = sourceCount + bufferSize + deviceCount + 1;

        if (prevTime == 0 || rightTime * TIME_TO_DISTANCE_FACTOR > WIDTH) {
            Line nextLine = new Line(prevTime * TIME_TO_DISTANCE_FACTOR, LINE_DISTANCE,
                    Math.max(WIDTH, rightTime * TIME_TO_DISTANCE_FACTOR), LINE_DISTANCE);
            for (int i = 0; i < linesCount; i++) {
                diagramPane.getChildren().add(nextLine);
                nextLine = new Line(nextLine.getStartX(), nextLine.getStartY() + LINE_DISTANCE,
                        nextLine.getEndX(), nextLine.getEndY() + LINE_DISTANCE);
            }
        }
    }

    private void drawCondition(CustomState state) {
        int sourceCount = CustomState.getSourceCount();
        int bufferSize = CustomState.getBufferCapacity();
        int deviceCount = CustomState.getDeviceCount();

        int failureShift = sourceCount + bufferSize + deviceCount + 1;
        int timeToDistance = (int) (TIME_TO_DISTANCE_FACTOR * state.getTime());

        List<Boolean> bufferConditions = state.getBufferStates();
        List<Boolean> deviceConditions = state.getDeviceStates();

        Integer deviceId = state.getDeviceId();
        Integer bufferId = state.getBufferId();
        String requestId = state.getRequestId();

        if (state.getSourceId() != null) {
            int srcId = state.getSourceId();
            int y1 = (srcId + 1) * LINE_DISTANCE;
            int y2 = y1 - TACT_HEIGHT;

            drawLine(timeToDistance, y1, timeToDistance, y2, COLOR_NORMAL);

            sign(timeToDistance, y2 - 15, String.valueOf(requestId));

            if (deviceId != null) {
                drawDashLineAndSign(timeToDistance, y1, (sourceCount + deviceId + 1) * LINE_DISTANCE,
                        String.valueOf(requestId));
            } else if (bufferId != null) {
                drawDashLineAndSign(timeToDistance, y1, (sourceCount + bufferSize + bufferId + 1) * LINE_DISTANCE,
                        String.valueOf(requestId));
            } else if (state.isFailed()) {
                drawDashLineAndSign(timeToDistance, y1, (sourceCount + bufferSize + deviceCount) * LINE_DISTANCE,
                        String.valueOf(requestId));
            }
        }

        int deviceShift = sourceCount + 1;
        for (int i = 0; i < deviceCount; i++) {
            boolean deviceCondition = deviceConditions.get(i);
            if (deviceCondition || deviceId != null) {
                if (deviceId != null && deviceId == i) {
                    int y1 = (deviceShift + i) * LINE_DISTANCE;
                    int y2 = y1 - TACT_HEIGHT;
                    drawLine(timeToDistance, y1, timeToDistance, y2, COLOR_EVENT);
                }

                if (lastStateId != 0) {
                    CustomState prevCondition = states.get(lastStateId - 1);
                    boolean prevDeviceCondition = prevCondition.getDeviceStates().get(i);

                    if (prevDeviceCondition) {
                        int prevTimeToDistance = (int) (TIME_TO_DISTANCE_FACTOR * prevCondition.getTime());
                        int y1 = (deviceShift + i) * LINE_DISTANCE - TACT_HEIGHT;
                        int y2 = y1;

                        drawLine(prevTimeToDistance, y1, timeToDistance, y2, COLOR_DEVICE);
                    }
                }
            }
        }

        int bufferShift = deviceShift + sourceCount;
        int lastFailureIndex = -1; // Инициализируем индекс последней заявки с отказом
        boolean hasFreeBufferSpace = bufferConditions.contains(false);
        for (int i = 0; i < bufferSize; i++) {
            boolean bufferCondition = bufferConditions.get(i);

            if (bufferCondition || bufferId != null) {
                if (bufferId != null && bufferId == i) {
                    int y1 = (bufferShift + i) * LINE_DISTANCE;
                    int y2 = y1 - TACT_HEIGHT;
                    drawLine(timeToDistance, y1, timeToDistance, y2, COLOR_EVENT);

                    if (state.getDeviceId() != null) {
                        drawDashLineAndSign(timeToDistance, (sourceCount + bufferId + 1) * LINE_DISTANCE,
                                (sourceCount + deviceId + 1) * LINE_DISTANCE,
                                String.valueOf(requestId));
                    }
                }

                if (lastStateId != 0) {
                    CustomState prevCondition = states.get(lastStateId - 1);
                    boolean prevBufferCondition = prevCondition.getBufferStates().get(i);
                    if (prevBufferCondition) {
                        lastFailureIndex = i; //
                        int prevTimeToDistance = (int) (TIME_TO_DISTANCE_FACTOR * prevCondition.getTime());
                        int y1 = (bufferShift + i) * LINE_DISTANCE - TACT_HEIGHT;
                        int y2 = y1;

                        drawLine(prevTimeToDistance, y1, timeToDistance, y2, COLOR_BUFFER);
                    }
                }
            }
        }

        if (state.isFailed()) {
            if (hasFreeBufferSpace) {
                // Поместить новую заявку (А) в буфер на место последней заявки с отказом (Б)
                if (lastFailureIndex != -1) {
                    // Освободить место в буфере для заявки (А)
                    bufferConditions.set(lastFailureIndex, false);

                    // Поместить новую заявку (А) на место предыдущей заявки с отказом (Б) в буфере
                    bufferConditions.set((bufferSize - 1), true);

                    // Визуализация новой заявки (А)
                    int y1New = (bufferShift + lastFailureIndex) * LINE_DISTANCE;
                    int y2New = y1New - TACT_HEIGHT;
                    drawLine(timeToDistance, y1New, timeToDistance, y2New, COLOR_EVENT);
                    CustomState prevCondition = states.get(lastStateId - 1);
                    int prevTimeToDistance = (int) (TIME_TO_DISTANCE_FACTOR * prevCondition.getTime());
                    int y1 = (bufferShift + lastFailureIndex) * LINE_DISTANCE - TACT_HEIGHT;
                    int y2 = y1;
                    drawLine(prevTimeToDistance, y1, timeToDistance, y2, COLOR_BUFFER);

                    if (state.getDeviceId() != null) {
                        drawDashLineAndSign(timeToDistance, (sourceCount + bufferSize) * LINE_DISTANCE,
                                (sourceCount + deviceCount + state.getDeviceId() + 1) * LINE_DISTANCE,
                                String.valueOf(requestId));
                    }
                }
            } else if (lastFailureIndex != -1) {
                CustomState prevCondition = states.get(lastStateId - 1);
                int y1New = (bufferShift + lastFailureIndex) * LINE_DISTANCE;
                    int y2New = y1New - TACT_HEIGHT;
                    drawLine(timeToDistance, y1New, timeToDistance, y2New, COLOR_EVENT);
                // Отправить последнюю заявку с отказом (Б) в отказ
                bufferConditions.set(lastFailureIndex, false); // Освободить место в буфере
                int y1 = (failureShift) * LINE_DISTANCE;
                int y2 = y1 - TACT_HEIGHT;
                drawLine(timeToDistance, y1, timeToDistance, y2, COLOR_FAILED);
                sign(timeToDistance, y2 + 15, String.valueOf(prevCondition.getRequestId()));
            }
        }

        Stage stage = (Stage) stepModePage.getWindow();
        stage.show();
    }

    private void drawDashLineAndSign(int x, int y1, int y2, String reqNumber) {
        Line dashLine = new Line(x, y1, x, y2);
        dashLine.getStrokeDashArray().addAll(2d, 2d);
        dashLine.setStroke(Color.INDIANRED);
        diagramPane.getChildren().add(dashLine);
        sign(x, y2 - 15, reqNumber);
    }

    private void drawLine(int x, int y1, int x2, int y2, Color color) {
        Line line = new Line(x, y1, x2, y2);
        line.setStroke(color);
        diagramPane.getChildren().add(line);
    }

    private void sign(int x, int y, String signStr) {
        Label label = new Label(signStr);
        label.setLayoutX(x);
        label.setLayoutY(y);
        diagramPane.getChildren().add(label);
    }

    @FXML
    public void finishButtonClick() {
        while (lastStateId != states.size() - 1) {
            nextButtonClick();
        }
        scrollPane.setHvalue(1);
    }
}
