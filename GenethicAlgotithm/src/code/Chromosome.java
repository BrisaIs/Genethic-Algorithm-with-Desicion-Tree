package code;

import java.util.Arrays;

public class Chromosome
{
	int [] chr;
	Double fitness;

	public Chromosome(int c[], Double f)
	{
		this.chr = c;//Verificar si es necesario declarar el new int[]
		this.fitness = f;
	}

	public void setChromosome(int chromosome [])
	{
		this.chr = chromosome;
		// for(int j = 0; j < this.chr.length; j++)
			// {this.chr[j] = chromosome[j];}
	}

	public void setFitness(double f) { this.fitness = new Double(f);}

	public int[] getChromosome(){ return this.chr;}
	public Double getFitness(){ return this.fitness;}

	@Override public String toString()
	{
		return fitness.toString()+","+Arrays.toString(this.chr);
	}
}
