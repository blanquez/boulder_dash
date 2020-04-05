package src_Blanquez_Perez_AntonioJose;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import tools.pathfinder.Node;

import java.util.ArrayList;
import java.util.Collections;

public class Agent extends AbstractPlayer{
	
	//Variables
	
	Vector2d fescala;
	Vector2d portal;
	Vector2d sol_path;
	boolean hayPlan;
	ArrayList<Types.ACTIONS> path;
	ArrayList<Observation>[] objetos_inamovibles;
	ArrayList<Vector2d> coord_inamovibles;
	ArrayList<Observation>[] gemas;
	ArrayList<Vector2d> coord_gemas;
	ArrayList<Boolean> coger_gemas;
	int cont_gemas;
	int mejor_ind_n5;
	
	//Constructor
	
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);   
        
        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        if(posiciones != null) {
        	portal = posiciones[0].get(0).position;
        	portal.x = Math.floor(portal.x/fescala.x);
        	portal.y = Math.floor(portal.y/fescala.y);
        }
        
        gemas = stateObs.getResourcesPositions();
        if(gemas != null) {
        	coger_gemas = new ArrayList<Boolean>();
        	for(int i=0;i<gemas[0].size();i++) coger_gemas.add(false);
        	coord_gemas = new ArrayList<Vector2d>();
        	for(int i=0; i<gemas[0].size();i++)
        		coord_gemas.add(new Vector2d(Math.floor(gemas[0].get(i).position.x/fescala.x), Math.floor(gemas[0].get(i).position.y/fescala.y)));
        }
        
        objetos_inamovibles = stateObs.getImmovablePositions();
        coord_inamovibles = new ArrayList<Vector2d>();
        for(int i=0; i<objetos_inamovibles[0].size();i++)
			coord_inamovibles.add(new Vector2d(Math.floor(objetos_inamovibles[0].get(i).position.x/fescala.x), Math.floor(objetos_inamovibles[0].get(i).position.y/fescala.y)));

        hayPlan=false;
        mejor_ind_n5 = -1;
        path = new ArrayList<Types.ACTIONS>();
	}
	
	//Clase nodo
	
	/*class nodo{
		Vector2d pos;
		int coste;
		int ori;
		double distancia;
		ArrayList<Types.ACTIONS> path;
		
		nodo(){
			pos = new Vector2d();
			coste = 0;
			ori = 0;
			path = new ArrayList<Types.ACTIONS>();
			distancia = 0;
		}
		
		nodo(Vector2d entrada){
			pos = entrada;
			coste = 0;
			ori = 0;
			path = new ArrayList<Types.ACTIONS>();
			distancia = calcular_distancia(pos);
		}
		
		public boolean equals(nodo o) {
			if (o.pos.x == pos.x && o.pos.y == pos.y) return true;
			else return false;
		}
		
		public void clone(nodo o) {
			pos = new Vector2d(o.pos);
			coste = o.coste;
			ori = o.ori;
			path = new ArrayList<Types.ACTIONS>();
			for(int i=0;i<o.path.size();i++) path.add(o.path.get(i));
			distancia = calcular_distancia(pos);
		}
	};*/
	
	// Métodos de clase
	
	// Calcular distancia
	
	public double calcular_distancia(Vector2d pos) {
		return Math.abs(pos.x-sol_path.x)+Math.abs(pos.y-sol_path.y);
	}
	
	// Buscar en vector y priority
	
	public int buscarEnVector(Node acomparar, ArrayList<Node> entrada) {
		for(int i=0; i<entrada.size();i++)
			if(acomparar.equals(entrada.get(i)))
					return i;
		return -1;
	}
	
	// Saber si una posición es un muro
	
	public boolean esMuro(Vector2d entrada) {
		for(int i=0;i<coord_inamovibles.size();i++) {
			if(entrada.x == coord_inamovibles.get(i).x && entrada.y == coord_inamovibles.get(i).y) return true;
		}
		return false;
	}
	
	// A*
	
	private ArrayList<ACTIONS> a_estrella(Node posicion_actual, StateObservation stateObs, ElapsedCpuTimer elapserTimer) {
		ArrayList<Node> cerrados = new ArrayList<Node>();
		ArrayList<Node> abiertos = new ArrayList<Node>();
		Node actual, hijo_up, hijo_down, hijo_left, hijo_right;
		int enCerrados = 0,enAbiertos = 0, mejor = 0;
		double coste_aux;
		cerrados.clear();
		abiertos.clear();
		
		abiertos.add(posicion_actual);
		
		do{
			if(elapserTimer.exceededMaxTime() || abiertos.isEmpty()) return null;
			
			//Buscar mejor
			actual = new Node(abiertos.get(0).position);
			coste_aux = abiertos.get(0).totalCost;
			actual.totalCost = coste_aux;
			actual.estimatedCost = calcular_distancia(actual.position);
			actual.parent = abiertos.get(0).parent;
			actual.comingFrom = new Vector2d(abiertos.get(0).comingFrom);
			mejor = 0;
			for(int i=1;i<abiertos.size();i++) {
				if(abiertos.get(i).totalCost + abiertos.get(i).estimatedCost < actual.totalCost + actual.estimatedCost) {
					actual = new Node(abiertos.get(i).position);
					coste_aux = abiertos.get(i).totalCost;
					actual.totalCost = coste_aux;
					actual.estimatedCost = calcular_distancia(actual.position);
					mejor = i;
				}
			}
			
			actual.parent = abiertos.get(mejor).parent;
			actual.comingFrom = new Vector2d(abiertos.get(mejor).comingFrom);
			
			abiertos.remove(mejor);
			cerrados.add(actual);
			
			//System.out.println("ACTUAL: " + actual.position + ", " + actual.totalCost + ", " + actual.estimatedCost);
			//if(actual.parent != null) System.out.print(", " + actual.comingFrom + ", " + actual.parent.position);
			//System.out.println();
			
			//System.out.println("CERRADOS");
			//for(int i=0;i<cerrados.size();i++) System.out.println(cerrados.get(i).position + " - " + cerrados.get(i).totalCost + " " + cerrados.get(i).estimatedCost);
			//System.out.println();
		
			
			hijo_up = new Node(new Vector2d(actual.position.x, actual.position.y - 1));
			hijo_down = new Node(new Vector2d(actual.position.x, actual.position.y + 1));
			hijo_left = new Node(new Vector2d(actual.position.x - 1, actual.position.y));
			hijo_right = new Node(new Vector2d(actual.position.x + 1, actual.position.y));
			
			hijo_up.parent = actual;
			hijo_down.parent = actual;
			hijo_left.parent = actual;
			hijo_right.parent = actual;
			
			hijo_up.setMoveDir(actual);
			hijo_down.setMoveDir(actual);
			hijo_left.setMoveDir(actual);
			hijo_right.setMoveDir(actual);
			
			//System.out.println("----------------------------------------------------");
			
			// Hijo Arriba
			if(!esMuro(hijo_up.position)) {
				if(actual.comingFrom.y == -1) coste_aux = actual.totalCost + 1;
				else coste_aux = actual.totalCost + 2;
				hijo_up.totalCost = coste_aux;
				hijo_up.estimatedCost = calcular_distancia(hijo_up.position);
				
				//System.out.println("UP: " + hijo_up.position + ", " + hijo_up.totalCost + ", " + hijo_up.estimatedCost);
				
				//System.out.println(hijo_up.totalCost);
				//System.out.println(hijo_up.comingFrom);
				
				enCerrados = buscarEnVector(hijo_up, cerrados);
				enAbiertos = buscarEnVector(hijo_up, abiertos);
				
				if(enAbiertos > -1) {
					if(hijo_up.totalCost + hijo_up.estimatedCost < abiertos.get(enAbiertos).totalCost + abiertos.get(enAbiertos).estimatedCost){
						abiertos.remove(enAbiertos);
						abiertos.add(hijo_up);
					}
				}
				else if(enCerrados > -1) {
					if(hijo_up.totalCost + hijo_up.estimatedCost < cerrados.get(enCerrados).totalCost + cerrados.get(enCerrados).estimatedCost){
						cerrados.remove(enCerrados);
						abiertos.add(hijo_up);
					}
				}
				else abiertos.add(hijo_up);
			}
			
			// Hijo Abajo
			if(!esMuro(hijo_down.position)) {
				if(actual.comingFrom.y == 1) coste_aux = actual.totalCost + 1;
				else coste_aux = actual.totalCost + 2;
				hijo_down.totalCost = coste_aux;
				hijo_down.estimatedCost = calcular_distancia(hijo_down.position);
				
				//System.out.println("DOWN: " + hijo_down.position + ", " + hijo_down.totalCost + ", " + hijo_down.estimatedCost);
				
				//System.out.println(hijo_down.totalCost);
				//System.out.println(hijo_down.comingFrom);
				
				enCerrados = buscarEnVector(hijo_down, cerrados);
				enAbiertos = buscarEnVector(hijo_down, abiertos);
				
				if(enAbiertos > -1) {
					if(hijo_down.totalCost + hijo_down.estimatedCost < abiertos.get(enAbiertos).totalCost + abiertos.get(enAbiertos).estimatedCost){
						abiertos.remove(enAbiertos);
						abiertos.add(hijo_down);
					}
				}
				else if(enCerrados > -1) {
					if(hijo_down.totalCost + hijo_down.estimatedCost < cerrados.get(enCerrados).totalCost + cerrados.get(enCerrados).estimatedCost){
						cerrados.remove(enCerrados);
						abiertos.add(hijo_down);
					}
				}
				else abiertos.add(hijo_down);
			}
			
			// Hijo izquierda
			if(!esMuro(hijo_left.position)) {
				if(actual.comingFrom.x == -1) coste_aux = actual.totalCost + 1;
				else coste_aux = actual.totalCost + 2;
				hijo_left.totalCost = coste_aux;
				hijo_left.estimatedCost = calcular_distancia(hijo_left.position);
				
				//System.out.println("LEFT: " + hijo_left.position + ", " + hijo_left.totalCost + ", " + hijo_left.estimatedCost);
				
				//System.out.println(hijo_left.totalCost);
				//System.out.println(hijo_left.comingFrom);
				
				enCerrados = buscarEnVector(hijo_left, cerrados);
				enAbiertos = buscarEnVector(hijo_left, abiertos);
				
				if(enAbiertos > -1) {
					if(hijo_left.totalCost + hijo_left.estimatedCost < abiertos.get(enAbiertos).totalCost + abiertos.get(enAbiertos).estimatedCost){
						abiertos.remove(enAbiertos);
						abiertos.add(hijo_left);
					}
				}
				else if(enCerrados > -1) {
					if(hijo_left.totalCost + hijo_left.estimatedCost < cerrados.get(enCerrados).totalCost + cerrados.get(enCerrados).estimatedCost){
						cerrados.remove(enCerrados);
						abiertos.add(hijo_left);
					}
				}
				else abiertos.add(hijo_left);
			}
			
			// Hijo derecha
			if(!esMuro(hijo_right.position)) {
				if(actual.comingFrom.x == 1) coste_aux = actual.totalCost + 1;
				else coste_aux = actual.totalCost + 2;
				hijo_right.totalCost = coste_aux;
				hijo_right.estimatedCost = calcular_distancia(hijo_right.position);
				
				//System.out.println("RIGHT: " + hijo_right.position + ", " + hijo_right.totalCost + ", " + hijo_right.estimatedCost);
				
				//System.out.println(hijo_right.totalCost);
				//System.out.println(hijo_right.comingFrom);
				
				enCerrados = buscarEnVector(hijo_right, cerrados);
				enAbiertos = buscarEnVector(hijo_right, abiertos);
				
				if(enAbiertos > -1) {
					if(hijo_right.totalCost + hijo_right.estimatedCost < abiertos.get(enAbiertos).totalCost + abiertos.get(enAbiertos).estimatedCost){
						abiertos.remove(enAbiertos);
						abiertos.add(hijo_right);
					}
				}
				else if(enCerrados > -1) {
					if(hijo_right.totalCost + hijo_right.estimatedCost < cerrados.get(enCerrados).totalCost + cerrados.get(enCerrados).estimatedCost){
						cerrados.remove(enCerrados);
						abiertos.add(hijo_right);
					}
				}
				else abiertos.add(hijo_right);
			}
			//System.out.println("ABIERTOS");
			//for(int i=0;i<abiertos.size();i++) System.out.println(abiertos.get(i).position + " - " + abiertos.get(i).totalCost + " " + abiertos.get(i).estimatedCost);
			//System.out.println();
			//System.out.println("-------------------------------");
			
		}while((actual.position.x != sol_path.x || actual.position.y != sol_path.y));
		
		ArrayList<Types.ACTIONS> unfixed_path = new ArrayList<Types.ACTIONS>();
		ArrayList<Types.ACTIONS> fixed_path = new ArrayList<Types.ACTIONS>();
		Vector2d from = new Vector2d();
		
		//Hacer el path
		
		while(actual.parent != null) {
			from = new Vector2d(actual.comingFrom);
			if(from.x == 0) {
				if(from.y == 1) unfixed_path.add(Types.ACTIONS.ACTION_DOWN);
				else unfixed_path.add(Types.ACTIONS.ACTION_UP);
			}else {
				if(from.x == 1) unfixed_path.add(Types.ACTIONS.ACTION_RIGHT);
				else unfixed_path.add(Types.ACTIONS.ACTION_LEFT);
			}
			actual = actual.parent;
		}
		
		Collections.reverse(unfixed_path);
		
		// Fixear path
		
		if(from.x != actual.comingFrom.x || from.y != actual.comingFrom.y) fixed_path.add(unfixed_path.get(0));
		fixed_path.add(unfixed_path.get(0));
		for(int i=1;i<unfixed_path.size();i++) {
			if(!unfixed_path.get(i).equals(unfixed_path.get(i-1))) fixed_path.add(unfixed_path.get(i));
			fixed_path.add(unfixed_path.get(i));
		}
		
		return fixed_path;
	}
	
	@Override
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapserTimer){
		
		Vector2d avatar =  new Vector2d(stateObs.getAvatarPosition().x / fescala.x, stateObs.getAvatarPosition().y / fescala.y);
		ArrayList<Observation>[] enemigos = stateObs.getNPCPositions();
		
		////////NIVEL 1 /////////////////////////////////////////////////////////////////////////
		
		if(enemigos == null && gemas == null) {
			
			if(!hayPlan) {
				Node inicial = new Node(avatar);
				
				sol_path = portal;
				
				inicial.comingFrom = new Vector2d(stateObs.getAvatarOrientation());
				inicial.estimatedCost = calcular_distancia(inicial.position);
				
				path.clear();
				path.addAll(a_estrella(inicial,stateObs,elapserTimer));
				if(path != null)hayPlan = true;
			}
			
			Types.ACTIONS sig_act;
			if(path != null) {
				sig_act = path.get(0);
				path.remove(0);
			}
			else sig_act = Types.ACTIONS.ACTION_NIL;
			return sig_act;
			
		//////// NIVEL 2 /////////////////////////////////////////////////////////////////////////
			
		}else if(enemigos == null && gemas != null) {
			
			if(path.size() < 1) hayPlan = false;
			
			if(!hayPlan) {
				int mejor_ind = -1;
				sol_path = avatar;
				double mejor = 9999;
				for(int i=0;i<coord_gemas.size() && stateObs.getGameScore() < 20;i++) {
					if(calcular_distancia(coord_gemas.get(i)) < mejor && !coger_gemas.get(i)) {
						mejor = calcular_distancia(coord_gemas.get(i));
						mejor_ind = i;
					}
				}
				if(mejor_ind != -1) {
					coger_gemas.set(mejor_ind, true);
					sol_path = coord_gemas.get(mejor_ind);
				}else sol_path = portal;
				
				
				Node inicial = new Node(avatar);
				
				inicial.comingFrom = new Vector2d(stateObs.getAvatarOrientation());
				inicial.estimatedCost = calcular_distancia(inicial.position);
		
				path.clear();
				path.addAll(a_estrella(inicial,stateObs,elapserTimer));
				if(path != null) hayPlan = true;
			}
			
			Types.ACTIONS sig_act;
			if(path != null) {
				sig_act = path.get(0);
				path.remove(0);
			}
			else sig_act = Types.ACTIONS.ACTION_NIL;
			return sig_act;
			
		//////// NIVEL 3 /////////////////////////////////////////////////////////////////////////
			
		}else if(enemigos[0].size() == 1 && gemas == null) {
			
			Vector2d posicion_enemigo = new Vector2d(enemigos[0].get(0).position.x / fescala.x, enemigos[0].get(0).position.y / fescala.y);
			Vector2d posicion_aux = new Vector2d(avatar);
			ArrayList<Double> movimientos = new ArrayList<Double>();
			movimientos.clear();
			
			//Mirar a la derecha
			posicion_aux.x = avatar.x + 1;
			posicion_aux.y = avatar.y;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
			
			//Mirar abajo
			posicion_aux.x = avatar.x;
			posicion_aux.y = avatar.y + 1;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
			
			//Mirar a la izquierda
			posicion_aux.x = avatar.x - 1;
			posicion_aux.y = avatar.y;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
			
			//Mirar arriba
			posicion_aux.x = avatar.x;
			posicion_aux.y = avatar.y - 1;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
			
			int ind_maximo = 0;
			double maximo = movimientos.get(0);
			for(int i=1;i<movimientos.size();i++) {
				if(movimientos.get(i) > maximo) {
					maximo = movimientos.get(i);
					ind_maximo = i;
				}
			}
						
			switch(ind_maximo) {
			case 0:
				return Types.ACTIONS.ACTION_RIGHT;
			case 1:
				return Types.ACTIONS.ACTION_DOWN;
			case 2:
				return Types.ACTIONS.ACTION_LEFT;
			case 3:
				return Types.ACTIONS.ACTION_UP;
			default:
				return Types.ACTIONS.ACTION_NIL;
			}
			
		//////// NIVEL 4 /////////////////////////////////////////////////////////////////////////
			
		}else if(enemigos[0].size() > 1 && gemas == null ) {
			
			Vector2d posicion_enemigo1 = new Vector2d(enemigos[0].get(0).position.x / fescala.x, enemigos[0].get(0).position.y / fescala.y);
			Vector2d posicion_enemigo2 = new Vector2d(enemigos[0].get(1).position.x / fescala.x, enemigos[0].get(1).position.y / fescala.y);
			Vector2d posicion_aux = new Vector2d(avatar);
			ArrayList<Double> movimientos = new ArrayList<Double>();
			movimientos.clear();
			
			//Mirar a la derecha
			posicion_aux.x = avatar.x + 1;
			posicion_aux.y = avatar.y;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add((Math.abs(posicion_enemigo1.x-posicion_aux.x)+Math.abs(posicion_enemigo1.y-posicion_aux.y)) * (Math.abs(posicion_enemigo2.x-posicion_aux.x)+Math.abs(posicion_enemigo2.y-posicion_aux.y)));
			
			//Mirar abajo
			posicion_aux.x = avatar.x;
			posicion_aux.y = avatar.y + 1;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add((Math.abs(posicion_enemigo1.x-posicion_aux.x)+Math.abs(posicion_enemigo1.y-posicion_aux.y)) * (Math.abs(posicion_enemigo2.x-posicion_aux.x)+Math.abs(posicion_enemigo2.y-posicion_aux.y)));
			
			//Mirar a la izquierda
			posicion_aux.x = avatar.x - 1;
			posicion_aux.y = avatar.y;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add((Math.abs(posicion_enemigo1.x-posicion_aux.x)+Math.abs(posicion_enemigo1.y-posicion_aux.y)) * (Math.abs(posicion_enemigo2.x-posicion_aux.x)+Math.abs(posicion_enemigo2.y-posicion_aux.y)));
			
			//Mirar arriba
			posicion_aux.x = avatar.x;
			posicion_aux.y = avatar.y - 1;
			
			if(esMuro(posicion_aux)) movimientos.add(-1.0);
			else movimientos.add((Math.abs(posicion_enemigo1.x-posicion_aux.x)+Math.abs(posicion_enemigo1.y-posicion_aux.y)) * (Math.abs(posicion_enemigo2.x-posicion_aux.x)+Math.abs(posicion_enemigo2.y-posicion_aux.y)));
			
			int ind_maximo = 0;
			double maximo = movimientos.get(0);
			for(int i=1;i<movimientos.size();i++) {
				if(movimientos.get(i) > maximo) {
					maximo = movimientos.get(i);
					ind_maximo = i;
				}
			}
						
			switch(ind_maximo) {
			case 0:
				return Types.ACTIONS.ACTION_RIGHT;
			case 1:
				return Types.ACTIONS.ACTION_DOWN;
			case 2:
				return Types.ACTIONS.ACTION_LEFT;
			case 3:
				return Types.ACTIONS.ACTION_UP;
			default:
				return Types.ACTIONS.ACTION_NIL;
			}
			
		//////// NIVEL 5 /////////////////////////////////////////////////////////////////////////	
			
		}else {
			Vector2d posicion_enemigo = new Vector2d(enemigos[0].get(0).position.x / fescala.x, enemigos[0].get(0).position.y / fescala.y);
			Vector2d posicion_aux = new Vector2d(avatar);
			ArrayList<Double> movimientos = new ArrayList<Double>();
			
			if(Math.abs(posicion_enemigo.x-avatar.x)+Math.abs(posicion_enemigo.y-avatar.y) < 5) {
				hayPlan = false;
				movimientos.clear();
				
				//Mirar a la derecha
				posicion_aux.x = avatar.x + 1;
				posicion_aux.y = avatar.y;
				
				if(esMuro(posicion_aux)) movimientos.add(-1.0);
				else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
				
				//Mirar abajo
				posicion_aux.x = avatar.x;
				posicion_aux.y = avatar.y + 1;
				
				if(esMuro(posicion_aux)) movimientos.add(-1.0);
				else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
				
				//Mirar a la izquierda
				posicion_aux.x = avatar.x - 1;
				posicion_aux.y = avatar.y;
				
				if(esMuro(posicion_aux)) movimientos.add(-1.0);
				else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
				
				//Mirar arriba
				posicion_aux.x = avatar.x;
				posicion_aux.y = avatar.y - 1;
				
				if(esMuro(posicion_aux)) movimientos.add(-1.0);
				else movimientos.add(Math.abs(posicion_enemigo.x-posicion_aux.x)+Math.abs(posicion_enemigo.y-posicion_aux.y));
				
				int ind_maximo = 0;
				double maximo = movimientos.get(0);
				for(int i=1;i<movimientos.size();i++) {
					if(movimientos.get(i) > maximo) {
						maximo = movimientos.get(i);
						ind_maximo = i;
					}
				}
							
				switch(ind_maximo) {
				case 0:
					return Types.ACTIONS.ACTION_RIGHT;
				case 1:
					return Types.ACTIONS.ACTION_DOWN;
				case 2:
					return Types.ACTIONS.ACTION_LEFT;
				case 3:
					return Types.ACTIONS.ACTION_UP;
				default:
					return Types.ACTIONS.ACTION_NIL;
				}
			}else {
				
				if(mejor_ind_n5 != -1 && avatar.x == coord_gemas.get(mejor_ind_n5).x && avatar.y == coord_gemas.get(mejor_ind_n5).y ) {
					coger_gemas.set(mejor_ind_n5, true);
					hayPlan = false;
				}
				
				if(!hayPlan) {
					path.clear();
					mejor_ind_n5 = -1;
					sol_path = avatar;
					double mejor = 9999;
					for(int i=0;i<coord_gemas.size() && stateObs.getGameScore() < 20;i++) {
						if(calcular_distancia(coord_gemas.get(i)) < mejor && !coger_gemas.get(i)) {
							mejor = calcular_distancia(coord_gemas.get(i));
							mejor_ind_n5 = i;
						}
					}
					if(mejor_ind_n5 != -1) sol_path = coord_gemas.get(mejor_ind_n5);
					else sol_path = portal;
					
					
					Node inicial = new Node(avatar);
					
					inicial.comingFrom = new Vector2d(stateObs.getAvatarOrientation());
					inicial.estimatedCost = calcular_distancia(inicial.position);
			
					path.addAll(a_estrella(inicial,stateObs,elapserTimer));
					if(path != null) hayPlan = true;
				}
				
				Types.ACTIONS sig_act;
				if(path.size() >= 1) {
					sig_act = path.get(0);
					path.remove(0);
				}else{
					sig_act = Types.ACTIONS.ACTION_NIL;
				}
				return sig_act;
			}
			
		}
	}

}
