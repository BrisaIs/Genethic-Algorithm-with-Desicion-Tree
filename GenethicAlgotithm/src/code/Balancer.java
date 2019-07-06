package code;

import java.util.*;

import java.io.*;

import weka.filters.*;
import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.evaluation.Evaluation;
import weka.core.converters.ConverterUtils.DataSource;

public class Balancer
{
	private String trainningSet [];
	private String testSet [];
	private int iterations, toMutate;
	private int  sizeT, sizeP, classes, attrs, size; //epocas, no. de instancias, no. de clases, atributos
	private ArrayList <Chromosome>  population = new ArrayList <Chromosome>();	//for store each's chromosome fitness
	private int indexClasses[]; //clases de los Indices
	private Random rnd;

	public Balancer(String trs, String tss, int mut, int iter)
	{
		int i = 0, j= 0;

		String ts [] = trs.split("\n");
		String ps  [] = tss.split("\n");

		this.iterations = iter;

		//Conjunto de entrenamiento
		this.sizeT = Integer.parseInt(clean(ts[0]));
		this.attrs = Integer.parseInt(clean(ts[1]));
		this.classes = Integer.parseInt(clean(ts[2]));

		this.trainningSet	= new String [sizeT];//Se crea la matriz: no. de instancias x [atributos + clase]

		for(i = 3; i < ts.length; i++)
			{ this.trainningSet[i - 3] = ts[i]; }

		//Conjunto de prueba
		this.sizeP = Integer.parseInt(clean(ps[0]));
		this.testSet	= new String[sizeP];//Se crea la matriz: no. de instancias x [atributos + clase]

		for(i = 3; i < ps.length; i++)
			{ this.testSet[i - 3] = ps[i]; }

 		this. rnd = new Random();
		//Otras inicializaciones
		this.size = 100;
		this. toMutate = mut;//  % del cromosoma a mutar

		//Poblacion
		int aux[] = new int[sizeT];
		for( i = 0; i < size; i++)
		{
			for(j = 0; j < sizeT; j++)
			{
				if(this.rnd.nextBoolean())
				 	{aux[j]= 1;}
				else
					aux[j] = 0;
			}
			this.population.add(new Chromosome(aux, testChromosome(aux)));
		}
		this.population.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
		//System.out.println(this.population.toString() + "\n");

		//Para obtener las clases por instancia del conjunto de prueba
		this.indexClasses = new int[sizeT];
		String [] aux1;
		for( i = 3; i < sizeT; i++)
		{
			aux1 = ts[i].split(",");
			this.indexClasses[i] = Integer.parseInt(clean(aux1[this.attrs]));//Guarda la clase de cada instancia
		}

	}

	public String balance()
	{
		String theBest = "";
		System.out.println("Starting Balance " + this.iterations+"\n");
		String balancePath = "C:/Users/Unicorn/Documents/Fac/AI/GenethicAlgotithm/src/code/balance.txt";
		String outputPath = "C:/Users/Unicorn/Documents/Fac/AI/GenethicAlgotithm/src/code/output.txt";

		for(int i = 0; i < this.iterations; i++)
		{
			crossover();
			System.out.println("Iteracion: " + i + " #bits: " + getChromosomeIndexes(this.population.get(this.size-1).getChromosome()).size() + " Fitness: " + this.population.get(this.size-1).getFitness() + "\n");
			System.out.println("Best chromosome: \n "+ stringBest(getChromosomeIndexes(this.population.get(this.size-1).getChromosome())) + "\n");
		}


		theBest += "Fitness P trainned with T = " + this.population.get(this.size-1).getFitness() + ".\n";
		double fitF = testBalanced(this.population.get(this.size-1).getChromosome());
		theBest +=  "Fitness P trainned with B " + fitF + "\n";
		theBest += "Best chromosome: \n "+ stringBest(getChromosomeIndexes(this.population.get(this.size-1).getChromosome())) + "\n";
		theBest += getDist(this.population.get(this.size-1).getChromosome());
		System.out.println("Ending Balance\n");

		return theBest;
	}
	private double testBalanced(int chromosome[])
	{
		int i = 0;
		Instances t = null, p = getTrainning(chromosome);
		Classifier m = new J48();
		Evaluation eval = null;
		double r = 0;
		int [] fullT = new int[sizeT];
		for(i =0; i <  sizeT; i ++)
			{fullT[i] = 1;}

		t = getTrainning(fullT);
		try
			{ m.buildClassifier(t); }
		catch (Exception ex)
			{ System.out.println("Failed to build model"); }

		// Evaluate classifier with test dataset
		try
		{
			eval = new Evaluation(t);
			eval.evaluateModel(m, p);
			r = eval.pctCorrect();
		 }
		catch (Exception ex)
			{ System.out.println("Failed to test model"); }

		return r;
	}

	//selecciona (crossRate*size)/2 individuos para cruzarlos
	private  void crossover()//single point crossover
	{

		int[] mask;
		int[] offspring1;
		int [] offspring1Mut;
		int[] offspring2;
		int[] offspring2Mut;
		double fit;

		mask = fillRandom();

		offspring1 = fillLikeMum(mask);
		offspring2 = fillLikeDad(mask);

		//mutacion de offspring
		offspring1Mut = mutate(offspring1);
		offspring2Mut = mutate(offspring2);

		//Solo va a integrar los offspring a la poblacion si sus fitness son mejores que los peores
		fit = testChromosome(offspring1Mut);

		if(this.population.get(0).getFitness() < fit)
		{
			System.out.println("Offs1: " + fit + "w: "+ this.population.get(0).getFitness()  + "\n" );
			this.population.remove(0);
			this.population.add( new Chromosome(offspring1Mut, fit));
			this.population.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
		}

		fit = testChromosome(offspring2Mut);

		if(this.population.get(0).getFitness() < fit)
		{
			System.out.println("Offs2: " + fit + "w: "+ this.population.get(0).getFitness()  + "\n" );
			this.population.remove(0);
			this.population.add(new Chromosome(offspring1Mut, fit));
			this.population.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
		}


	}

	private double testChromosome(int chromosome[])
	{
		int i = 0;
		Instances t = null, p = getTest();
		Classifier m = new J48();
		Evaluation eval = null;
		double r = 0;

		t = getTrainning(chromosome);
		try
			{ m.buildClassifier(t); }
		catch (Exception ex)
			{ System.out.println("Failed to build model"); }

		// Evaluate classifier with test dataset
		try
		{
		   eval = new Evaluation(t);
		   eval.evaluateModel(m, p);
			r = eval.pctCorrect();
		 }
		catch (Exception ex)
			{ System.out.println("Failed to test model"); }

		return r;
	}

	private int[] fillLikeMum(int mask[])
	{
		int []mum = this.population.get(this.size-1).getChromosome();
		int [] dad = this.population.get(this.size-2).getChromosome();
		int [] offspring =  new int[this.sizeT];

		for(int i = 0; i < this.sizeT; i++)
		{
			if(mask[i] == 1)
				{offspring [i] = mum[i];}
			else
				{offspring [i] = dad[i];}
		}

		return offspring;
	}

	private int[] fillLikeDad(int mask[])
	{
		int []mum = this.population.get(this.size-1).getChromosome();
		int [] dad = this.population.get(this.size-2).getChromosome();
		int [] offspring =  new int[this.sizeT];

		for(int i = 0; i < this.sizeT; i++)
		{
			if(mask[i] == 1)
				{offspring [i] = dad[i];}
			else
				{offspring [i] = mum[i];}
		}

		return offspring;
	}

	private int [] fillRandom()
	{
		int[] chromosome = new int[this.sizeT];
		for(int j = 0; j < chromosome.length; j++)
		{
			if(this.rnd.nextBoolean())
				{chromosome[j] = 1;}
			else
				{chromosome[j] = 0;}
		}

		return chromosome;
	}

	//Selecciona n cromosomas y les cambia 1 bit aleatorio
	private int[] mutate(int chromosome[])
	{
		List<Bucketita> mutClasses = getMutationIndex(chromosome);
		System.out.println(mutClasses);
		ArrayList<Integer> indexes = getChromosomeIndexes(chromosome);
		int percentage = 0;
		List<Integer> ofAClass = new ArrayList<Integer>();

		for(int i = 0; i < mutClasses.size(); i++)
		{
			//Mutacion
			for(int k = 0; k < indexes.size(); k++)
			{
				if(this.indexClasses[indexes.get(k)] == mutClasses.get(i).clases)
					ofAClass.add( new Integer(indexes.get(k)) );
			}

			percentage =  (int) Math.round( (double)this.toMutate*(double)ofAClass.size()/100.0 );
			Collections.shuffle(ofAClass);
			for(int j = 0; j < percentage; j++)
				chromosome[ ofAClass.get(j) ] = 0;//Solo los 1 por 0's

			ofAClass = new ArrayList<Integer>();
		}

		return chromosome;

	}

	private List<Bucketita> getMutationIndex(int chromosome[])
	{
		ArrayList <Bucketita> bucketota = new ArrayList <Bucketita>();
		ArrayList<Integer> indexes = getChromosomeIndexes(chromosome);
		int [] cls = new int[this.classes];	for(int i = 0; i < cls.length; i++)	cls[i] = 0;

		for(int i = 0; i < indexes.size(); i++)
			cls[ this.indexClasses[indexes.get(i)] ] ++;

		for(int i = 0; i < this.classes; i++)
			bucketota.add(new Bucketita(i, cls[i]));

		bucketota.sort((o1, o2) -> o1.insts.compareTo(o2.insts));

		int index = 0, dif = 0, temp;
		for(int i = 0; i < this.classes-1 ; i++)
		{
			temp = Math.abs(bucketota.get(i).insts - bucketota.get(i + 1).insts);
			if(dif < temp)
			{
				dif = temp;
				index = i+1;
			}

		}

		return bucketota.subList(index, bucketota.size());
		// System.out.println("bucketota: " + bucketota.toString() + " div: "  + dif +  "val: " + val + "\n");
	}

	private String getDist(int chromosome[])
	{
		String s = "";
		ArrayList<Integer> indexes = getChromosomeIndexes(chromosome);
		int [] cls = new int[this.classes];
		int i;

		for(i = 0; i < indexes.size(); i++)
		{
			cls[this.indexClasses[indexes.get(i)]] ++;
		}

		for( i = 0; i < this.classes; i++)
			{s += "Clase:  " + i + "  Instancias: " +  cls[i]  + "\n"; }
		return s;
	}

	private ArrayList <Integer> getChromosomeIndexes(int chromosome[])
	{
		ArrayList <Integer> selected = new ArrayList <Integer>(); //Indices de los 1's de cada cromosoma(se va reciclando)

		for(int i = 0; i < this.sizeT; i++)
		{
			if(chromosome[i] == 1)
				{ selected.add(i); }
		}

		return selected;
	}

	//Obtiene un T' a partir del cromosoma
	private Instances getTrainning(int chromosome[])
	{
		DataSource source;
		Instances inst = null;
		String path = "C:/Users/Unicorn/Documents/Fac/AI/GenethicAlgotithm/src/code/trainning.arff";
		String atrs = "", clas = "@attribute class {", p = "";
		ArrayList <Integer> selected = getChromosomeIndexes(chromosome);
		int i;

		for(i = 0; i < selected.size(); i++)
			{ p += this.trainningSet[selected.get(i)] + "\n";}

		for(i = 0; i < this.attrs; i++)
			{ atrs += "@attribute " + i + " real \n"; }

		for(i = 0; i < this.classes; i++)
		{
			if(i == this.classes - 1)
				{ clas += i + "}\n"; }
			else
			 	{clas += i + ",";}
		 }

		 arff_writer(path, "@relation TrainningSet", atrs, clas, p);
		 //System.out.println(path +  "@relation TrainningSet" + atrs + clas + p);
		 try
		 {
			 // source = new DataSource(path);
			 // inst = source.getDataSet();
			 // inst.setClassIndex(inst.numAttributes() - 1); //question
			BufferedReader datafile = readDataFile(path);
			 inst = new Instances(datafile);
	 		inst.setClassIndex(inst.numAttributes() - 1);
		 }
		 catch(Exception e){ System.out.println("Unable to get trainning instances.");}

		return inst;
	}

	//Obtiene las instancias del conjunto de entrenamiento
	private Instances getTest()
	{
		DataSource source;
		Instances inst = null;
		String path = "C:/Users/Unicorn/Documents/Fac/AI/GenethicAlgotithm/src/code/test.arff";
		String atrs = "", clas = "@attribute class {";
		int i;

		for(i = 0; i < this.attrs; i++)
			{ atrs += "@attribute " + i + " real \n"; }

		for(i = 0; i < this.classes; i++)
		{
			if(i == this.classes - 1)
				{ clas += i + "}\n"; }
			else
			 	{clas += i + ",";}
		 }

		 arff_writer(path, "@relation TestSet", atrs, clas, stringTestSet());

		 try
		 {
			 // source = new DataSource(path);
			 // inst = source.getDataSet();
			 // inst.setClassIndex(inst.numAttributes() - 1); //question

			BufferedReader datafile = readDataFile(path);
 			inst = new Instances(datafile);
 	 		inst.setClassIndex(inst.numAttributes() - 1);
		 }
		 catch(Exception e){ System.out.println("Unable to get test instances.");}

		return inst;
	}

	private BufferedReader readDataFile(String filename)
	{
		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

	private void appendResults(String fileName, int i, double fit, String best)
	{
		BufferedWriter writer = null;
		try
		 {
		//	 FileWriter fw = new FileWriter(fileName, true);
			 //BufferedWriter bw = new BufferedWriter(fw);
			 writer = new BufferedWriter(
	                     new FileWriter( fileName, true ));
			 writer.write("Iteration: " + i + " Best Fitness: " + fit + "\n" + best + "\n");
			 writer.newLine();
			 writer.flush();	//Ensure that all has been written
			 writer.close();
		}
		catch(Exception e){ System.out.println("Failed to append results\n");}
	}

	public static void arff_writer( String filename, String rel,  String attribs, String clas, String inst )
   {
     BufferedWriter writer = null;
     try
     {
       writer = new BufferedWriter(
                     new FileWriter( filename ));
		writer.write(rel+"\n");
		writer.write(attribs);
		writer.write(clas);
		writer.write("@data\n");
		 writer.write( inst);
       writer.newLine();
       writer.flush();	//Ensure that all has been written
       writer.close();
     }
     catch(IOException e){ System.out.println(e); }
     finally{ try { writer.close(); } catch (Exception ex) {/*ignore*/} }
   }

	private  String clean(String toClean)
	{
		String spaicy = toClean.replaceAll("\\s","");
		String taby = spaicy.replaceAll("\\t","");
		String cleany = taby.replaceAll("\r\n","");

		return cleany;
	}

	private String stringBest(ArrayList <Integer> best )
	{
		String r = "";
		for(int i = 0; i < best.size(); i++)
		{
			if(i < best.size()-1 )
				{r += best.get(i) + ", ";}
			else
				{r += best.get(i) + "\n";}
		}

		return r;
	}

	private String stringTrainningSet()
	{
		String s = "";

		for(int i = 0; i < this.sizeT; i++)
			{ s += this.trainningSet[i] + "\n" ; }

		return s;
	}

	private String stringTestSet()
	{
		String s = "";

		for(int i = 0; i < this.sizeP; i++)
			{  s += this.testSet[i] + "\n" ; }

		return s;
	}

}
