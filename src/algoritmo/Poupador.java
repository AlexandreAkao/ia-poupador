package algoritmo;

import controle.Constantes;

import java.util.*;

enum Moves {
    Stay(0),
    Up(1),
    Down(2),
    Right(3),
    Left(4);

    private final int value;

    Moves(int valorOpcao) {
        value = valorOpcao;
    }

    public int getValue() {
        return value;
    }
}

class Step {
    private final int stepsTo;
    private final int[] positions;

    Step(int stepsTo, int[] positions) {
        this.stepsTo = stepsTo;
        this.positions = positions;
    }

    public int getStepsTo() {
        return stepsTo;
    }

    public int[] getPositions() {
        return positions;
    }
}

class StepMemento {
    private final int stepsTo;
    private final int posX;
    private final int posY;

    StepMemento(int stepsTo, int posX, int posY) {
        this.stepsTo = stepsTo;
        this.posX = posX;
        this.posY = posY;
    }

    public int getStepsTo() {
        return stepsTo;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
}

public class Poupador extends ProgramaPoupador {
    private final int[][] visionSteps = {
            {7, 11, 12, 16},
            {2, 6, 8, 10, 13, 15, 17, 21},
            {1, 3, 5, 9, 14, 18, 20, 22},
            {0, 4, 19, 23}
    };
    private final int[][] moveHelper = {
            {-2, -2}, {-1, -2}, {0, -2}, {1, -2}, {2, -2},
            {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {2, -1},
            {-2, 0}, {-1, 0}, {1, 0}, {2, 0},
            {-2, 1}, {-1, 1}, {0, 1}, {1, 1}, {2, 1},
            {-2, 2}, {-1, 2}, {0, 2}, {1, 2}, {2, 2},
    };

    private int[][] memento = new int[30][30];

    Poupador() {
        Arrays.stream(memento).forEach(tile -> Arrays.fill(tile, -3));
    }

    public int acao() {
        this.memorialize();
        return this.movimentation();
    }

    private void memorialize() {
        int posX = (int) this.sensor.getPosicao().getX();
        int posY = (int) this.sensor.getPosicao().getY();
        int[] vision = this.sensor.getVisaoIdentificacao();

        for (int i = 0; i < vision.length; i++) {
            if (vision[i] != Constantes.foraAmbiene) {
                int newPosX = posX + moveHelper[i][0];
                int newPosY = posY + moveHelper[i][1];
                memento[posX][posY] = 10; // ja passou
                memento[newPosX][newPosY] = vision[i];
            }
        }
    }

    private List<Integer> nearbyCoin() {
        return nearbyCode(Constantes.numeroMoeda);
    }

    private List<Integer> nearbyPowerUp() {
        return nearbyCode(Constantes.numeroPastinhaPoder);
    }

    private List<Integer> nearbyLadrao() {
        Set<Integer> set = new LinkedHashSet<>();
        List<Integer> ladrao01 = nearbyCode(Constantes.numeroLadrao01);
        List<Integer> ladrao02 = nearbyCode(Constantes.numeroLadrao02);
        List<Integer> ladrao03 = nearbyCode(Constantes.numeroLadrao03);
        List<Integer> ladrao04 = nearbyCode(Constantes.numeroLadrao04);
        if (!ladrao01.contains(-1)) set.addAll(ladrao01);
        if (!ladrao02.contains(-1)) set.addAll(ladrao02);
        if (!ladrao03.contains(-1)) set.addAll(ladrao03);
        if (!ladrao04.contains(-1)) set.addAll(ladrao04);

        List<Integer> combined = new ArrayList<>(set);
        if (combined.size() == 0) combined.add(-1);

        return combined;
    }

    /**
     * Position of code
     *
     * @param code Vision code to search
     * @return Return a list of nearby Code
     */
    private List<Integer> nearbyCode(int code) {
        List<Integer> steps = new ArrayList<>();
        steps.add(-1);
        for (int[] levelStep : visionSteps) {
            Arrays.stream(levelStep).forEach(pos -> {
                int codePos = this.sensor.getVisaoIdentificacao()[pos];
                if (codePos == code) {
                    if (steps.contains(-1)) steps.clear();
                    steps.add(pos);
                }
            });

            if (!steps.contains(-1)) break;
        }
        return steps;
    }

    private Step stepsTo(List<Integer> positionsTo) {
        for (int i = 0; i < visionSteps.length; i++) {
            int[] levelStep = visionSteps[i];
            List<Integer> positions = new ArrayList<>();
            for (int pos : levelStep) {
                if (positionsTo.contains(pos)) positions.add(pos);
            }
            if (positions.size() != 0) {
                int[] positionsInt = positions.stream().mapToInt(integer -> integer).toArray();
                return new Step(i + 1, positionsInt);
            }

        }
        int[] emptyArray = {};
        return new Step(-1, emptyArray);
    }

    private Step stepsToCoin() {
        return stepsTo(nearbyCoin());
    }

    private Step stepsToPowerUp() {
        return stepsTo(nearbyPowerUp());
    }

    private Step stepsToLadrao() {
        return stepsTo(nearbyLadrao());
    }

    private boolean isTopWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

        int[] topVision = {
                vision[0], vision[1], vision[2], vision[3], vision[4],
                vision[5], vision[6], vision[7], vision[8], vision[9],
        };

        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
    }

    private boolean isBottomWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

        int[] topVision = {
                vision[14], vision[15], vision[16], vision[17], vision[18],
                vision[19], vision[20], vision[21], vision[22], vision[23],
        };

        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
    }

    private boolean isLeftWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

        int[] topVision = {
                vision[0], vision[1], vision[5], vision[6], vision[10],
                vision[11], vision[14], vision[15], vision[19], vision[20],
        };

        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
    }

    private boolean isRightWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

        int[] topVision = {
                vision[3], vision[4], vision[8], vision[9], vision[12],
                vision[13], vision[17], vision[18], vision[22], vision[23],
        };

        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
    }

    private int random() {
        int random = (int) (Math.random() * 5);

        if (
                random == Moves.Up.getValue() && isTopWall() ||
                random == Moves.Down.getValue() && isBottomWall() ||
                random == Moves.Left.getValue() && isLeftWall() ||
                random == Moves.Right.getValue() && isRightWall()
        ) {
            return random();
        }

        System.out.println("random -- " + random);
        return random;
    }

    private int remember(int code) {
        int posX = (int) this.sensor.getPosicao().getX();
        int posY = (int) this.sensor.getPosicao().getY();
        int[] vision = this.sensor.getVisaoIdentificacao();

        List<StepMemento> memory = new ArrayList<>();

        for (int i = 0; i < memento.length; i++) {
            for (int j = 0; j < memento[i].length; j++) {
                if (memento[i][j] == code) {
                    int stepsTo = Math.abs(posX - i) + Math.abs(posY - j);
                    memory.add(new StepMemento(stepsTo, i, j));
                }
            }
        }

        memory.sort(Comparator.comparingInt(StepMemento::getStepsTo));

        for (StepMemento sm : memory) {
            int x = Integer.compare(sm.getPosX(), posX);
            int y = Integer.compare(sm.getPosY(), posY);

            if (x == 0) {
                if (vision[16] == Constantes.numeroParede || vision[7] == Constantes.numeroParede) {
                    if (vision[11] == Constantes.numeroParede) {
                        System.out.println("remember1 1 -- " + Moves.Right.getValue());
                        return Moves.Right.getValue();
                    }
                    System.out.println("remember2 1 -- " + Moves.Left.getValue());
                    return Moves.Left.getValue();
                }
                System.out.println("remember3 1 -- " + (y == 1 ? Moves.Down.getValue() : Moves.Up.getValue()));
                return y == 1 ? Moves.Down.getValue() : Moves.Up.getValue();
            } else if (y == 0 || (y == 1 && vision[16] == Constantes.numeroParede) || (y == -1 && vision[7] == Constantes.numeroParede)) {
                if (vision[11] == Constantes.numeroParede || vision[12] == Constantes.numeroParede) {
                    if (vision[16] == Constantes.numeroParede) {
                        System.out.println("remember1 2 -- " + Moves.Up.getValue());
                        return Moves.Up.getValue();
                    }
                    System.out.println("remember2 2 -- " + Moves.Down.getValue());
                    return Moves.Down.getValue();
                }
                System.out.println("remember3 2 -- " + (x == 1 ? Moves.Right.getValue() : Moves.Left.getValue()));
                return x == 1 ? Moves.Right.getValue() : Moves.Left.getValue();
            }
        }

        return random();
    }

    private int movimentation() {
        Step stepsToCoin = stepsToCoin();
        Step stepsToPowerUp = stepsToPowerUp();
        Step stepsToLadrao = stepsToLadrao();

        int moveToCoin = moveTo(stepsToCoin.getPositions());
        System.out.println("moveToCoin -- " + moveToCoin);
        if (moveToCoin == -1) {
            return remember(Constantes.numeroMoeda);
        }
        return moveToCoin;
    }

    private int moveUp(int position, int[] vision) {
        if (vision[7] == Constantes.numeroParede) {
            if (position == 0 || position == 1 || position == 5 || position == 6) {
                if (vision[11] != Constantes.numeroParede) {
                    return Moves.Left.getValue();
                } else if (vision[12] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else {
                    return Moves.Down.getValue();
                }
            } else if (position == 3 || position == 4 || position == 8 || position == 9) {
                if (vision[12] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else if (vision[11] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else {
                    return Moves.Down.getValue();
                }
            } else {
                return (int) (Math.random() * 5);
            }
        }

        if ((position == 4 || position == 9) && vision[8] == Constantes.numeroParede) {
            return Moves.Right.getValue();
        } else if ((position == 0 || position == 5) && vision[6] == Constantes.numeroParede) {
            return Moves.Right.getValue();
        }

        return Moves.Up.getValue();
    }

    private int moveDown(int position, int[] vision) {
        if (vision[16] == Constantes.numeroParede) {
            if (position == 14 || position == 15 || position == 19 || position == 20) {
                if (vision[11] != Constantes.numeroParede) {
                    return Moves.Left.getValue();
                } else if (vision[12] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else {
                    return Moves.Up.getValue();
                }
            } else if (position == 17 || position == 18 || position == 22 || position == 23) {
                if (vision[12] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else if (vision[11] != Constantes.numeroParede) {
                    return Moves.Right.getValue();
                } else {
                    return Moves.Up.getValue();
                }
            } else {
                return (int) (Math.random() * 5);
            }
        }

        if ((position == 18 || position == 23) && vision[17] == Constantes.numeroParede) {
            return Moves.Right.getValue();
        } else if ((position == 14 || position == 19) && vision[15] == Constantes.numeroParede) {
            return Moves.Right.getValue();
        }

        return Moves.Down.getValue();
    }

    private int moveLeft(int position, int[] vision) {
        if (vision[11] == Constantes.numeroParede) {
            if (vision[6] != Constantes.numeroParede) {
                return Moves.Up.getValue();
            } else if (vision[15] != Constantes.numeroParede) {
                return Moves.Down.getValue();
            } else {
                return Moves.Right.getValue();
            }
        }

        return Moves.Left.getValue();
    }

    private int moveRight(int position, int[] vision) {
        if (vision[12] == Constantes.numeroParede) {
            if (vision[8] != Constantes.numeroParede) {
                return Moves.Up.getValue();
            } else if (vision[17] != Constantes.numeroParede) {
                return Moves.Down.getValue();
            } else {
                return Moves.Left.getValue();
            }
        }

        return Moves.Right.getValue();
    }

    private int moveTo(int[] positions) {
        int[] vision = this.sensor.getVisaoIdentificacao();

        for (int position : positions) {
            if (0 <= position && position <= 9) {
                return moveUp(position, vision);
            } else if (14 <= position && position <= 23) {
                return moveDown(position, vision);
            } else if (position == 10 || position == 11) {
                return moveLeft(position, vision);
            } else if (position == 12 || position == 13) {
                return moveRight(position, vision);
            } else {
                return -1;
            }
        }
        return -1;
    }
}