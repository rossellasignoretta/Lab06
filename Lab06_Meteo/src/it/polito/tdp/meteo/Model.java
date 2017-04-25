package it.polito.tdp.meteo;

import java.util.ArrayList;
import java.util.List;
import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 50;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	List<Citta> citta;
	
	public Model() {

	}

	public String getUmiditaMedia(int mese) {
		String risultato="UMIDITA' MEDIA NEL MESE SELEZIONATO\n";
		
		MeteoDAO dao=new MeteoDAO();
		Double UmiditaTorino=dao.getAvgRilevamentiLocalitaMese(mese, "Torino");
		Double UmiditaMilano=dao.getAvgRilevamentiLocalitaMese(mese,"Milano");
		Double UmiditaGenova=dao.getAvgRilevamentiLocalitaMese(mese,"Genova");
		
		risultato+="Torino "+UmiditaTorino+"\n";
		risultato+="Milano "+UmiditaMilano+"\n";
		risultato+="Genova "+UmiditaGenova;
		
		return risultato;
	}

	public String trovaSequenza(int mese) {
		String risultato="SEQUENZA OTTIMALE\n";
		
		MeteoDAO dao= new MeteoDAO();
		Citta Torino= new Citta("Torino",dao.getAllRilevamentiLocalitaMese(mese, "Torino"));
		Citta Milano= new Citta("Milano",dao.getAllRilevamentiLocalitaMese(mese, "Milano"));
		Citta Genova= new Citta("Genova",dao.getAllRilevamentiLocalitaMese(mese, "Genova"));
		
		citta=new ArrayList<Citta>();
		citta.add(Torino);
		citta.add(Milano);
		citta.add(Genova);
		
		List<SimpleCity> parziale= new ArrayList<SimpleCity>();
		List<SimpleCity> best= new ArrayList<SimpleCity>();

		recursive(parziale, best, 1);
		
		for(SimpleCity stemp: best){
			risultato+=stemp.toString()+"\n";
		}
		return risultato;
	}

	/**
	 * Metodo ricorsivo
	 * @param parziale
	 * @param best
	 * @param livello
	 */
	private void recursive(List<SimpleCity> parziale, List<SimpleCity> best, int livello) {
		//CONDIZIONE DI TERMINAZIONE
		if(parziale.size()==NUMERO_GIORNI_TOTALI){
			if (controllaCandidata(parziale) && punteggioSoluzione(parziale)<punteggioSoluzione(best)){
				best.clear();
				best.addAll(parziale) ;
				System.out.println(best);
				}
			
		}
		
		for(Citta ctemp: citta){
				Rilevamento r=ctemp.getRilevamento(livello);
				if (r!=null){
					int costo=r.getUmidita()*COST;
					SimpleCity c=new SimpleCity(ctemp.getNome(), costo);
					parziale.add(c);
						//System.out.println(parziale.toString());
					if(controllaParziale(parziale)){
						recursive(parziale, best, livello+1);	
					}
					parziale.remove(c);
				}	
		}
	}

	/**
	 * Calcola il punteggio delle soluzioni
	 * @param soluzioneCandidata
	 * @return
	 */
	private Double punteggioSoluzione(List<SimpleCity> soluzioneCandidata) {
		double score = 0.0;
		if(soluzioneCandidata.size()==0)//calcola punteggio best vuota
			return Double.MAX_VALUE;
		for(int i=0; i<soluzioneCandidata.size(); i++){
			SimpleCity a=soluzioneCandidata.get(i);
			score+=a.getCosto();
			if(i>0 && !a.getNome().equals(soluzioneCandidata.get(i-1).getNome())){
				score+=100;
			}
		}
		return score;
	}

	/**
	 * Verifica che il tecnico rimanga per almeno 3 giorni consecutivi nella stessa citta 
	 * e in tutto massimo 6 giorni per ogni citta
	 * @param parziale
	 * @return
	 */
	private boolean controllaParziale(List<SimpleCity> parziale) {
		//max 6 giorni nella stessa citta
		for(Citta c: citta){
			int count=0;
			for (SimpleCity s: parziale){
				if(s.getNome().equals(c.getNome())){
					count++;
				}
			}
			if (count>NUMERO_GIORNI_CITTA_MAX){
				return false;
			}
		}
		
		//min 3 giorni consecutivi nella stessa citta
		if(parziale.size()>1){
			SimpleCity citta= parziale.get(0);
			int count=0;
			for(SimpleCity s: parziale){
				if(citta.equals(s)){
					count++;
				}else{
					if (count>=NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN){
						citta=s;
						count=1;
					}else{
						return false;
					}
				}
			}
			
		}
		/*int size=parziale.size();
		if(size>1 && !parziale.get(size-1).equals(parziale.get(size-2))){
			if(size<=3)
				return false;
			else if(!parziale.get(size-2).equals(parziale.get(size-3)) || !parziale.get(size-2).equals(parziale.get(size-4)))
					return false;
		}*/
		
		
		return true;
		
		
		
		
	}
	/**
	 * Verifica che tutte le citta siano presenti nella soluzione candidata
	 * @param parziale
	 * @return 
	 */
	private boolean controllaCandidata(List<SimpleCity> parziale) {
		for(Citta c: citta){
			boolean presente=false;;
			for (SimpleCity s: parziale){
				if(s.getNome().equals(c.getNome()))
					presente=true;
			}
			if (presente==false)
				return false;
		}
		return true;
		
	}
	
	//CHE MI SERVE IL CONTATORE IN CITTA???

}
