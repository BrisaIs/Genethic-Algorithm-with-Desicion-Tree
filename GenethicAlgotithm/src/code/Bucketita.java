package code;

public class Bucketita
{
	public Integer clases;
	public Integer insts;

	public Bucketita( Integer c, Integer ins)
	{
		this.clases = c;
		this.insts = ins;
	}

	@Override public String toString()
	{
		return insts.toString();
	}
}
