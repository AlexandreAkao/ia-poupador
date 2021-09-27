package algoritmo;

import controle.Constantes;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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

class Coordinate {
    private final Point point;
    private final int move;
    private final List<Coordinate> adj;
    private final Coordinate parent;

    Coordinate(Point point, int move, Coordinate parent) {
        this.point = point;
        this.move = move;
        this.parent = parent;
        this.adj = new ArrayList<>();
    }

    public int getMove() {
        return move;
    }

    public Point getPoint() {
        return point;
    }

    public List<Coordinate> getAdj() {
        return adj;
    }

    public void addAdj(Coordinate newCoordinate) {
        this.adj.add(newCoordinate);
    }

    public Coordinate getParent() {
        return parent;
    }
}

class PointGraph {
    private final Point point;
    private final int stepsTo;


    PointGraph(Point point, int stepsTo) {
        this.point = point;
        this.stepsTo = stepsTo;
    }

    public Point getPoint() {
        return point;
    }

    public int getStepsTo() {
        return stepsTo;
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

    private final int[][] moves = {
            {0, 0}, {0, -1}, {0, 1}, {1, 0}, {-1, 0}
    };

    private final int[] visionHelper = {
            7, 16, 12, 11
    };

    private final int[][] possibleRange = {
            {0,-2},{0,-1},{1,-1},{1,0},{1,1},{2,0},{-1,-1},{-1,0},{-1,1},{2,0}
    };


    private final int[] visionMove = {0, 7, 16, 11, 12};

    private final List<Integer> lastSteps = new ArrayList<>(Collections.nCopies(8, 0));

    private List<Point> visionPosition = new ArrayList<>();
    private List<Point> memoryCoin = new ArrayList<>();
    private List<Point> memoryUnvisited = new ArrayList<>();
    private List<Point> memoryBank = new ArrayList<>();

    private final int[][] memento = new int[30][30];

    Poupador() {
        Arrays.stream(memento).forEach(tile -> Arrays.fill(tile, -3));
    }

    public int acao() {
        this.memorialize();
        int move = this.movimentation();
        setLastSteps(move);
        return move;
    }


    private void collectCoins(){
//        System.out.println(this.sensor.getNumeroDeMoedas());
        //verificar quando estiver perto do banco e se tiver maior que 0, guardar
        for (Point posicaoBanco : memoryBank) {
            if (posicaoBanco.getX() == 8 && posicaoBanco.getY() == 8 && sensor.getNumeroDeMoedas() > 0) {
                System.out.println("cuida em ir pro banco, guardei:" + this.sensor.getNumeroDeMoedas());
            }

        }
    }

    private void setLastSteps(int newStep) {
        lastSteps.set(7, lastSteps.get(6));
        lastSteps.set(6, lastSteps.get(5));
        lastSteps.set(5, lastSteps.get(4));
        lastSteps.set(4, lastSteps.get(3));
        lastSteps.set(3, lastSteps.get(2));
        lastSteps.set(2, lastSteps.get(1));
        lastSteps.set(1, lastSteps.get(0));
        lastSteps.set(0, newStep);
    }

    private void memorialize() {
        int posX = (int) this.sensor.getPosicao().getX();
        int posY = (int) this.sensor.getPosicao().getY();
        Point posAtual = sensor.getPosicao();
        int[] vision = this.sensor.getVisaoIdentificacao();
        memento[posX][posY] = 10;
        memoryCoin.remove(posAtual);
        for (int i = 0; i < vision.length; i++) {
            int newPosX = posX + moveHelper[i][0];
            int newPosY = posY + moveHelper[i][1];
            if (vision[i] != Constantes.foraAmbiene) {
                Point newPoint = new Point(newPosX, newPosY);
                if(vision[i] == Constantes.numeroMoeda && !memoryCoin.contains(newPoint)) {
                    memoryCoin.add(newPoint);
                }

                if(vision[i] == -3 && !memoryUnvisited.contains(newPoint)) {
                    memoryUnvisited.add(newPoint);
                }

                if(vision[i] == Constantes.numeroBanco && !memoryBank.contains(new Point(newPosX, newPosY))) {
                    System.out.println("pos do banco é:" + new Point(newPosX, newPosY));
                    memoryBank.add(newPoint);
                }

                if (memento[newPosX][newPosY] != 10) memento[newPosX][newPosY] = vision[i];

                if (vision[i] != Constantes.semVisao && vision[i] != Constantes.numeroPastinhaPoder) {
                    visionPosition.add(newPoint);
                }
            }
        }
        memoryCoin.sort((p1, p2) -> {
            int stepsToP1 = (int) (Math.abs(posX - p1.getX()) + Math.abs(posY - p1.getY()));
            int stepsToP2 = (int) (Math.abs(posX - p2.getX()) + Math.abs(posY - p2.getY()));

            return stepsToP1 - stepsToP2;
        });
    }

    private List<Integer> nearbyCoin() {
        return nearbyCode(Constantes.numeroMoeda);
    }

    private List<Integer> nearbyPowerUp() {
        return nearbyCode(Constantes.numeroPastinhaPoder);
    }

    private List<Integer> nearbyBank() {
        return nearbyCode(Constantes.numeroBanco);
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

    private Step stepsToBank() {
        return stepsTo(nearbyBank());
    }

    private boolean isTopWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

//        int[] topVision = {
//                vision[0], vision[1], vision[2], vision[3], vision[4],
//                vision[5], vision[6], vision[7], vision[8], vision[9],
//        };
//
//        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);

        return vision[7] == Constantes.numeroParede;
    }

    private boolean isBottomWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

//        int[] topVision = {
//                vision[14], vision[15], vision[16], vision[17], vision[18],
//                vision[19], vision[20], vision[21], vision[22], vision[23],
//        };
//
//        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
        return vision[16] == Constantes.numeroParede;
    }

    private boolean isLeftWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

//        int[] topVision = {
//                vision[0], vision[1], vision[5], vision[6], vision[10],
//                vision[11], vision[14], vision[15], vision[19], vision[20],
//        };
//
//        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);
        return vision[11] == Constantes.numeroParede;
    }

    private boolean isRightWall() {
        int[] vision = this.sensor.getVisaoIdentificacao();

//        int[] topVision = {
//                vision[3], vision[4], vision[8], vision[9], vision[12],
//                vision[13], vision[17], vision[18], vision[22], vision[23],
//        };
//
//        return Arrays.stream(topVision).allMatch(tile -> tile == Constantes.foraAmbiene);

        return vision[12] == Constantes.numeroParede;
    }

    private boolean isInLoop() {
        return lastSteps.get(0).equals(lastSteps.get(2))
                && lastSteps.get(0).equals(lastSteps.get(4))
                && lastSteps.get(0).equals(lastSteps.get(6))
                && lastSteps.get(1).equals(lastSteps.get(3))
                && lastSteps.get(1).equals(lastSteps.get(5))
                && lastSteps.get(1).equals(lastSteps.get(7));
    }

    private int randomWithout(int[] excludes) {
        int posX = (int) this.sensor.getPosicao().getX();
        int posY = (int) this.sensor.getPosicao().getY();

        int random = (int) (Math.random() * 4) + 1;
        if (excludes.length == 4) return random;
        int[] moveTo = this.moves[random];
        int newPosX = posX + moveTo[0];
        int newPosY = posY + moveTo[1];

        int finalRandom = random;
        boolean alreadyHave = Arrays.stream(excludes).anyMatch(move -> move == finalRandom);

        while (
            newPosX == 30 ||
            newPosY == 30 ||
            newPosX == -1 ||
            newPosY == -1 ||
            alreadyHave
        ) {
            random = (int) (Math.random() * 4) + 1;
            moveTo = this.moves[random];
            newPosX = posX + moveTo[0];
            newPosY = posY + moveTo[1];
            int finalRandom1 = random;
            alreadyHave = Arrays.stream(excludes).anyMatch(move -> move == finalRandom1);
            if ((newPosX == 30 || newPosY == 30 || newPosX == -1 || newPosY == -1) && !alreadyHave) {
                return random;
            }
//            System.out.println(newPosX + " ---- " + newPosY + " ----- " + alreadyHave);
        }

        Integer[] excludesParse = Arrays.stream(excludes).boxed().toArray(Integer[]::new);
        ArrayList<Integer> newRandom = new ArrayList<>(Arrays.asList(excludesParse));
        newRandom.add(random);
        int[] arrayInt = newRandom.stream().mapToInt(i -> i).toArray();

//        System.out.println(newPosX + " ---- " + newPosY + " ----- " + this.memento[newPosX][newPosY] + " ---- " + Arrays.toString(arrayInt) + " ---- " + random);
        if (this.memento[newPosX][newPosY] == 10) randomWithout(arrayInt);

        if (
            random == Moves.Up.getValue() && isTopWall() ||
            random == Moves.Down.getValue() && isBottomWall() ||
            random == Moves.Left.getValue() && isLeftWall() ||
            random == Moves.Right.getValue() && isRightWall()
        ) {
            return randomWithout(arrayInt);
        }

//        System.out.println("random -- " + random);
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

        if (isInLoop()) return randomWithout(new int[]{});

        memory.sort(Comparator.comparingInt(StepMemento::getStepsTo));

        for (StepMemento sm : memory) {
            int x = Integer.compare(sm.getPosX(), posX);
            int y = Integer.compare(sm.getPosY(), posY);

            if (x == 0) {
                if (vision[16] == Constantes.numeroParede && y == 1) {
                    if (vision[11] == Constantes.numeroParede) {
                        return Moves.Right.getValue();
                    } else if (vision[12] == Constantes.numeroParede) {
                        return Moves.Left.getValue();
                    }
                    return Moves.Left.getValue();
                } else if (vision[7] == Constantes.numeroParede && y == -1) {
                    if (vision[11] == Constantes.numeroParede) {
                        return Moves.Right.getValue();
                    } else if (vision[12] == Constantes.numeroParede) {
                        return Moves.Left.getValue();
                    }
                    return Moves.Left.getValue();
                }
                return y == 1 ? Moves.Down.getValue() : Moves.Up.getValue();
            } else if (
                    y == 0 ||
                            (y == 1 && vision[16] == Constantes.numeroParede) ||
                            (y == -1 && vision[7] == Constantes.numeroParede)
            ) {
                if (vision[11] == Constantes.numeroParede && x == -1) {
                    if (vision[16] == Constantes.numeroParede) {
                        return Moves.Up.getValue();
                    } else if (vision[7] == Constantes.numeroParede) {
                        return Moves.Down.getValue();
                    }
                    return Moves.Down.getValue();
                } else if (vision[12] == Constantes.numeroParede && x == 1) {
                    if (vision[16] == Constantes.numeroParede) {
                        return Moves.Up.getValue();
                    } else if (vision[7] == Constantes.numeroParede) {
                        return Moves.Down.getValue();
                    }
                    return Moves.Down.getValue();
                }
                return x == 1 ? Moves.Right.getValue() : Moves.Left.getValue();
            }
        }

        if (code != Constantes.posicaoLivre) {
            return remember(Constantes.posicaoLivre);
        }

        return randomWithout(new int[]{});
    }

    private int deposity() {
        Point point = this.sensor.getPosicao();
        int x, y;
        boolean find = false;
        List<Integer> movements = new ArrayList<>();
        LinkedList<Coordinate> queue = new LinkedList<>();
        List<Point> visited = new ArrayList<>();

        if (isInLoop() || memoryBank.size() == 0) return remember(Constantes.numeroBanco);

        Coordinate end = new Coordinate(this.memoryBank.get(0), 0, null);

        Coordinate aux;
        Coordinate currentPos = new Coordinate(point, 0, null);
        visited.add(currentPos.getPoint());
        queue.add(currentPos);

        while (!queue.isEmpty()) {
            aux = queue.pop();

            if (memoryBank.contains(aux.getPoint())) {
                find = true;
                end = aux;
                break;
            }

            for (int i = 1; i < this.moves.length; i++) {
                x = (int) (aux.getPoint().getX() + this.moves[i][0]);
                y = (int) (aux.getPoint().getY() + this.moves[i][1]);
                Point newPoint = new Point(x, y);

                if (
                        visionPosition.contains(newPoint) &&
                                !visited.contains(newPoint) &&
                                this.memento[x][y] != Constantes.numeroParede &&
                                this.memento[x][y] != Constantes.numeroPastinhaPoder &&
                                this.memento[x][y] != Constantes.numeroBanco &&
                                this.memento[x][y] != Constantes.numeroPoupador01 &&
                                this.memento[x][y] != Constantes.numeroPoupador02
                ) {
                    Coordinate n = new Coordinate(newPoint, i, aux);
                    visited.add(n.getPoint());
                    queue.add(n);
                }
            }
        }

        if (find) {
            while (end.getParent() != null) {
                movements.add(end.getMove());
                end = end.getParent();
            }

            return movements.get(movements.size() - 1);
        }


        return remember(Constantes.numeroBanco);
    }

    private int explore(int code) {
        Point point = this.sensor.getPosicao();
        int x, y;
        boolean find = false;
        List<Point> memory = code == -3 ? memoryUnvisited : memoryCoin;
        List<Integer> movements = new ArrayList<>();
        LinkedList<Coordinate> queue = new LinkedList<>();
        List<Point> visited = new ArrayList<>();

        if (isInLoop() || memory.size() == 0) return remember(code);

        Coordinate end = new Coordinate(memory.get(memory.size() - 1), 0, null);

        Coordinate aux;
        Coordinate currentPos = new Coordinate(point, 0, null);
        visited.add(currentPos.getPoint());
        queue.add(currentPos);

        while (!queue.isEmpty()) {
            aux = queue.pop();

            if (memory.contains(aux.getPoint())) {
                find = true;
                end = aux;
                break;
            }

            for (int i = 1; i < this.moves.length; i++) {
                x = (int) (aux.getPoint().getX() + this.moves[i][0]);
                y = (int) (aux.getPoint().getY() + this.moves[i][1]);
                Point newPoint = new Point(x, y);

                if (
                        visionPosition.contains(newPoint) &&
                                !visited.contains(newPoint) &&
                                this.memento[x][y] != Constantes.numeroParede &&
                                this.memento[x][y] != Constantes.numeroPastinhaPoder &&
                                this.memento[x][y] != Constantes.numeroBanco &&
                                this.memento[x][y] != Constantes.numeroPoupador01 &&
                                this.memento[x][y] != Constantes.numeroPoupador02
                ) {
                    Coordinate n = new Coordinate(newPoint, i, aux);
                    visited.add(n.getPoint());
                    queue.add(n);
                }
            }
        }

        if (find) {
            while (end.getParent() != null) {
                movements.add(end.getMove());
                end = end.getParent();
            }

            return movements.get(movements.size() - 1);
        }


        return remember(code);
    }

    // LADRAO

    private int avoidLadrao() {
        Map<Integer, Integer> posicaoOlfato = new HashMap<>();
        posicaoOlfato.put(6, 0);
        posicaoOlfato.put(7, 1);
        posicaoOlfato.put(8, 2);
        posicaoOlfato.put(11, 3);
        posicaoOlfato.put(12, 4);
        posicaoOlfato.put(15, 5);
        posicaoOlfato.put(16, 6);
        posicaoOlfato.put(17, 7);

        Map<Integer, Moves[]> movementOptions = new HashMap<>();
        movementOptions.put(7, new Moves[]{Moves.Down, Moves.Right, Moves.Left});
        movementOptions.put(12, new Moves[]{Moves.Left, Moves.Up, Moves.Down});
        movementOptions.put(11, new Moves[]{Moves.Right, Moves.Up, Moves.Down});
        movementOptions.put(16, new Moves[]{Moves.Up, Moves.Left, Moves.Right});
        movementOptions.put(6, new Moves[]{Moves.Down, Moves.Right} );
        movementOptions.put(8, new Moves[]{Moves.Down, Moves.Left});
        movementOptions.put(15, new Moves[]{Moves.Up, Moves.Right});
        movementOptions.put(17, new Moves[]{Moves.Up, Moves.Left});

        Map<Integer, Integer> movementVision = new HashMap<>();
        movementVision.put(Moves.Down.getValue(), 16);
        movementVision.put(Moves.Up.getValue(), 7);
        movementVision.put(Moves.Left.getValue(), 11);
        movementVision.put(Moves.Right.getValue(), 12);

        List<Integer> paredes = new ArrayList<>();
        int[] visao = this.sensor.getVisaoIdentificacao();
        List<Integer> posicaoLadroes = new ArrayList<>();
        for (int i = 0; i < visao.length; i++) {
            if ( visao[i] == 1 ) { paredes.add(i); }
            if ( visao[i] >= 200 ) { posicaoLadroes.add(i); }
        }


        List<Moves> safeMovements = new ArrayList<>();
        for (int i = 0; i < posicaoLadroes.size(); i++) {
            Integer posicao = posicaoLadroes.get(i);
            if (!movementOptions.containsKey(posicao)) {
                continue;
            }
            Moves[] options = movementOptions.get(posicao);
            for (Moves moveOption: options) {
                int mp = movementVision.get(moveOption.getValue());
                if (visao[movementVision.get(moveOption.getValue())] == Constantes.numeroMoeda) {
                    return moveOption.getValue();
                }
                if(!(paredes.contains(mp))) {
                    if(!(posicaoLadroes.contains(mp))) {
                        safeMovements.add(moveOption);
                    }
                }
            }
            List<Integer> possibleMoves = new ArrayList<>();
            // Obter a mais safe Possivel
            for (Moves move: safeMovements) {
                int[] olfato = this.sensor.getAmbienteOlfatoPoupador();
                int cheiro = olfato[posicaoOlfato.get(movementVision.get(move.getValue()))];

                if (visao[movementVision.get(move.getValue())] == Constantes.numeroMoeda) {
                    System.out.println("OPA MOEDA!! "+ move.getValue());
                    return move.getValue();
                }

                if (visao[movementVision.get(move.getValue())] == Constantes.foraAmbiene) {
                    continue;
                }

                if (visao[movementVision.get(move.getValue())] == Constantes.numeroPastinhaPoder) {
                    if (this.sensor.getNumeroDeMoedas() >= 5) {
                        System.out.println("OPA PASTILHA!! " + move.getValue());
                        return move.getValue();
                    }
                }

                if (visao[movementVision.get(move.getValue())] == Constantes.numeroParede) {
                    continue;
                }

                if (cheiro >= 2 || cheiro == 0 ) {
                    possibleMoves.add(move.getValue());
                }
            }

            if(possibleMoves.size() > 0) {
                int rnd = new Random().nextInt(possibleMoves.size());
                int move = possibleMoves.get(rnd);
                System.out.printf("(POSSIBLE MOVES) LADRAO NA POS %s DESVIADO! %s\n", posicao, move);
                return move;
            }

            if(safeMovements.size() > 0) {
                int rnd = new Random().nextInt(safeMovements.size());
                int move = safeMovements.get(rnd).getValue();
                System.out.printf("(POSSIBLE MOVES) LADRAO NA POS %s DESVIADO! %s\n", posicao, move);
                return move;
            }
        }

        for (int i = 1; i <= 5; i++) {
            int pos = visao[movementVision.get(i)];
            if ((pos <= 200) && (pos != 1) && (pos != -1)) {
                if ((pos == Constantes.numeroPastinhaPoder) && !(this.sensor.getNumeroDeMoedas() >= 5)) {
                    continue;
                }

                if(movementVision.get(i) == 2) {
                    if(visao[21] <= 200 && visao[16] <= 200) {
                        System.out.println("FALLBACK DE FUGIR" + pos);
                        return i;
                    }
                }

                if(movementVision.get(i) == 3) {
                    if(visao[10] <= 200 && visao[11] <= 200) {
                        return i;
                    }
                }

                if(movementVision.get(i) == 4) {
                    if(visao[12] <= 200 && visao[13] <= 200) {
                        return i;
                    }
                }

                if(movementVision.get(i) == 1) {
                    if(visao[2] <= 200 && visao[7] <= 200) {
                        return i;
                    }
                }
                return i;
            }
        }

        for (int i = 1; i <= 5; i++) {
            int pos = visao[movementVision.get(i)];
            if ((pos <= 200) && (pos != 1) && (pos != -1)) {
                if ((pos == Constantes.numeroPastinhaPoder) && !(this.sensor.getNumeroDeMoedas() >= 5)) {
                    continue;
                }
                return i;
            }
        }

        return 0;
    }

    private boolean checkForLadrao(Step ladrao) {
        if (ladrao.getStepsTo() == -1) return false;
        return ladrao.getStepsTo() < 3;
    }

    // LADRAO

    private int movimentation() {
        int[] vision = this.sensor.getVisaoIdentificacao();

        Step stepsToCoin = stepsToCoin();
        Step stepsToPowerUp = stepsToPowerUp();
        Step stepsToLadrao = stepsToLadrao();
        Step stepsToBank = stepsToBank();

        if (stepsToBank.getStepsTo() != -1 && this.sensor.getNumeroDeMoedas() > 0) {
            return deposity();
        } else if (checkForLadrao(stepsToLadrao)) {
            return avoidLadrao();
        }

        return explore(Constantes.numeroMoeda);
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