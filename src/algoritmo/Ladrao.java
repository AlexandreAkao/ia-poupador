package algoritmo;

import java.awt.*;
import java.util.LinkedList;
import java.util.Random;

public class Ladrao extends ProgramaLadrao {

	private LinkedList<Position> positions = new LinkedList<>();
	private Point positionNow;
	private int greaterWeight;
	private int direction;
	private Position position;

	public int acao() {
		int view = searchVison(); // procurar poupador na visão.
		int direct = searchSaverNoOlfato();
		positionNow = sensor.getPosicao();
		addPontVisited(positionNow);
		if(view != 0) {
			System.out.println("Sentido: Pursuit Sensor: Direction View: " + view);
			return view;
		} else if(direct != 0) {
			System.out.println("Sentido: Pursuit Sensor: Smell Direction: " + direct);
			return direct;
		} else {
			direction = explorar();
			System.out.println("Sentido: Exploration Direction: " + direction);
			return direction;
		}
	}

	// exploração
	private int explorar() {
		int[] visao = sensor.getVisaoIdentificacao();
		int[] direcoes = new int[5];
		direcoes[1] = evaluateMovement(visao, 1);
		direcoes[2] = evaluateMovement(visao, 2);
		direcoes[3] = evaluateMovement(visao, 3);
		direcoes[4] = evaluateMovement(visao, 4);
		Random random = new Random();
		greaterWeight = 0;
		direction = 1 + random.nextInt(4);
		for(int i = 1; i < direcoes.length; i++) {
			if(direcoes[i] >= greaterWeight) {
				greaterWeight = direcoes[i];
				direction = i;
			}
		}
		return direction;
	}

	// olhos view
	private int searchVison() {
		int[] view = sensor.getVisaoIdentificacao();
		int[] weight = {0, 0, 0, 0, 0};
		for(int i = 0; i < view.length; i++) {
			if(i <= 9) {
				weight[1] = getCodView(view[i]);
			} else if(i <= 11) {
				weight[4] = getCodView(view[i]);
			} else if(i <= 13) {
				weight[3] = getCodView(view[i]);
			} else if(i <= 23) {
				weight[2] = getCodView(view[i]);
			}
		}
		greaterWeight = 0;
		direction = 0;
		for(int i = 1; i < weight.length; i++) {
			if(weight[i] > greaterWeight) {
				greaterWeight = weight[i];
				direction = i;
			}
		}
		return direction;
	}

	private int getCodView(int view) {
		int weight = 0;
		if(view == 100) {
			weight += 1;
		}
		if(view == 200) {
			weight -= 2;
		}
		return weight;
	}

	private int searchSaverNoOlfato() {
		int[] smell = sensor.getAmbienteOlfatoPoupador();
		int[] weight = new int[5];
		for(int i = 0; i < smell.length; i++) {
			if(smell[i] >= 1) {
				if (i <= 2) {
					weight[1] = getOlfato(weight[1], smell[i]);
				} else if (i <= 3) {
					weight[4] = getOlfato(weight[4], smell[i]);
				} else if (i <= 4) {
					weight[3] = getOlfato(weight[3], smell[i]);
				} else if (i <= 7) {
					weight[2] = getOlfato(weight[2], smell[i]);
				}
			}
		}
		greaterWeight = 5;
		direction = 0;
		for(int i = 1; i < weight.length; i++) {
			if(weight[i] <= greaterWeight && weight[i] != 0) {
				greaterWeight = weight[i];
				direction = i;
			}
		}
		return direction;
	}

	// inteligencia - avaliar o moviento
	private int evaluateMovement(int[] view, int direction) {
		int codigoPoint = 10;
		int weight = 0;
		Point pointMap = new Point();
		if(direction == 1) {
			codigoPoint = view[7];
			pointMap = new Point(positionNow.x, positionNow.y - 1);
		} else if(direction == 2) {
			codigoPoint = view[16];
			pointMap = new Point(positionNow.x, positionNow.y + 1);
		} else if(direction == 3) {
			codigoPoint = view[12];
			pointMap = new Point(positionNow.x + 1, positionNow.y);
		} else if(direction == 4) {
			codigoPoint = view[11];
			pointMap = new Point(positionNow.x - 1, positionNow.y);
		}
		weight -= searchPontVisited(pointMap);
		if(codigoPoint == 1 || codigoPoint == 4 || codigoPoint == -2 || codigoPoint == -1 || codigoPoint == 3 || codigoPoint == 5) {
			weight -= 1;
		} else if(codigoPoint == 0) {
			weight += 3;
		}
		return weight;
	}

	// avaliador
	private int getOlfato(int weightPrevious, int newWeight)	{
		if(newWeight < weightPrevious && newWeight != 0 || weightPrevious == 0) {
			return newWeight;
		}
		return weightPrevious;
	}

	// memoria
	private void addPontVisited(Point point) {
		boolean found = false;
		for(int i = 0; i < positions.size(); i++) {
			position = positions.get(i);
			if(position.x == point.x && position.y == point.y) {
				positions.get(i).setPeso(position.getPeso() + 2);
				found = true;
			}
		}
		if(!found) {
			positions.add(new Position(point.x, point.y, 1));
		}
	}

	// memoria
	private int searchPontVisited(Point point) {
		for(int i = 0; i < positions.size(); i++) {
			position = positions.get(i);
			if(position.x == point.x && position.y == point.y) {
				return position.getPeso();
			}
		}
		return 0;
	}
}

class Movement {
	private int direcao;
	private int peso;

	public Movement(int direcao, int peso) {
		this.direcao = direcao;
		this.peso = peso;
	}

	public int getDirecao() {
		return direcao;
	}

	public void setDirecao(int direcao) {
		this.direcao = direcao;
	}

	public int getPeso() {
		return peso;
	}

	public void setPeso(int peso) {
		this.peso = peso;
	}
}


class Position extends Point {
	private int peso;

	public Position(int x, int y, int peso) {
		super(x, y);
		this.peso = peso;
	}

	public int getPeso() {
		return peso;
	}

	public void setPeso(int peso) {
		this.peso = peso;
	}
}

