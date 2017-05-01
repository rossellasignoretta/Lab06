package it.polito.tdp.meteo;

import java.util.ArrayList;
import java.util.List;
import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;

	List<Citta> citta;
	MeteoDAO dao= null;
	
	
	List<SimpleCity> best;
	private double punteggioMiglioreSoluzione;
	
	public Model() {
		
	}

	public String getUmiditaMedia(int mese) {
		String risultato="UMIDITA' MEDIA NEL MESE SELEZIONATO\n";
		
		dao=new MeteoDAO();
		
		for (String s : dao.getCities()){
			risultato+=s+" "+dao.getAvgRilevamentiLocalitaMese(mese, s)+"\n";

		}
		
		return risultato;
	}

	public String trovaSequenza(int mese) {
		String risultato="SEQUENZA OTTIMALE\n";
		
		dao= new MeteoDAO();
		citta=new ArrayList<Citta>();
		
		for (String s : dao.getCities())
			citta.add(new Citta(s, dao.getAllRilevamentiLocalitaMese(mese, s)));
		
		List<SimpleCity> parziale= new ArrayList<SimpleCity>();

		best= new ArrayList<SimpleCity>();
		punteggioMiglioreSoluzione = Double.MAX_VALUE;

		recursive(parziale, 0);
		
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
	private void recursive(List<SimpleCity> parziale, int livello) {
		//CONDIZIONE DI TERMINAZIONE
		if(livello>=NUMERO_GIORNI_TOTALI){
			double punteggioParziale=punteggioSoluzione(parziale);
			if (controllaCandidata(parziale) && punteggioParziale<punteggioMiglioreSoluzione){
				best.clear();
				best.addAll(parziale);
				punteggioMiglioreSoluzione=punteggioParziale;
				}
			return;
		}
		
		for(Citta ctemp: citta){
				Rilevamento r=ctemp.getRilevamenti().get(livello);
				int costo=r.getUmidita();
				//System.out.println(ctemp.getNome()+" "+r.getData()+" "+costo );
				SimpleCity c=new SimpleCity(ctemp.getNome(), costo);
					
				parziale.add(c);
				ctemp.increaseCounter();
						//System.out.println(parziale.toString());
				if(controllaParziale(parziale)){
					recursive(parziale, livello+1);	
					}
				parziale.remove(livello);
				ctemp.decreaseCounter();
					
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
				score+=COST;
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
			if (c.getCounter()>NUMERO_GIORNI_CITTA_MAX){
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

	
}
