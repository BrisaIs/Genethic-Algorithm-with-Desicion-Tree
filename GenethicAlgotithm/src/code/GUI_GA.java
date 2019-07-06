package code;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;

public class GUI_GA extends JFrame implements ActionListener
{
	private JPanel pBase, pTrainning, pTest, pVariables, pRun;
	private JButton btTest, btTrainning, btRun, btReset;
	private JTextField tfTest, tfTrainning, tfMutation, tfIterations;
	private JLabel lbTrainning, lbTest, lbCrossover, lbMutation, lbIterations, lbOutput;
	private JTextArea  taMsgs;
	private JScrollPane spMsgs;

	public String trainning="", test="", mutation, iterations;
	public int iter,mut;

	public GUI_GA()
	{
		super("Genethic Algorithm");
		this.setLocation(400,  200);

		this.trainning = "";
		this.test = "";
		this.mutation = "";
		this.iterations = "";
		this.mut = 0;
		this.iter = 0;

		pBase = new JPanel();
		pBase.setLayout(new BoxLayout(pBase,BoxLayout.PAGE_AXIS ));

		pTrainning = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pTest = new JPanel(new FlowLayout(FlowLayout.LEADING));
		pVariables = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pRun = new JPanel(new FlowLayout(FlowLayout.CENTER));

		ImageIcon load = convertImage("/images/open.png");
		ImageIcon run = convertImage("/images/play.png");
		ImageIcon reset = convertImage("/images/reset.png");

		btTest = new JButton(load);
		btTest.addActionListener(this);

		btRun = new JButton("Balance",run);
		btRun.addActionListener(this);

		btReset = new JButton("Reset", reset);
		btReset.addActionListener(this);

		this.btTrainning = new JButton(load);
		this.btTrainning.addActionListener(this);

		taMsgs = new JTextArea("");
		taMsgs.setEditable(true);
		taMsgs.setSize(750, 50);
		spMsgs = new JScrollPane(taMsgs);

		lbTrainning = new JLabel("Select Trainning Set:");
		lbTest = new JLabel("Select Test Set:        ");
		lbMutation = new JLabel("Set mutation percentage:");
		lbIterations  = new JLabel("Set a number of iterations:");
		lbOutput = new JLabel("Output:");

		tfTrainning  = new JTextField(50);
		tfTest  = new JTextField(50);
		tfMutation  = new JTextField(2);
		tfIterations = new JTextField(4);

		pTrainning.add(lbTrainning);
		pTrainning.add(tfTrainning);
		pTrainning.add(btTrainning);

		pTest.add(lbTest);
		pTest.add(tfTest);
		pTest.add(btTest);

		pVariables.add(lbMutation);
		pVariables.add(tfMutation);
		pVariables.add(lbIterations);
		pVariables.add(tfIterations);

		pRun.add(btRun);
		pRun.add(btReset);

		pBase.add(pTrainning);
		pBase.add(pTest);
		pBase.add(pVariables);
		pBase.add(pRun);
		pBase.add(lbOutput);
		pBase.add(spMsgs);
		add(pBase);
		pack();
	}

	private ImageIcon convertImage(String path)
	{
		ImageIcon origen = new ImageIcon(getClass().getResource(path));
		Image conversion = origen.getImage();
		Image tam = conversion.getScaledInstance(20, 15, Image.SCALE_SMOOTH);
		ImageIcon fin = new ImageIcon(tam);

		return fin;
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == btRun)
		{
			this.tfMutation.setEditable(false);
			this.tfIterations.setEditable(false);

			this.mutation = this.tfMutation.getText();
			this.iterations = this.tfIterations.getText();

			if(check());
			{
				System.out.println("Fuck" + this.iter+"\n");
				//this.taMsgs.append("\nSelected instances:\n");
				Balancer b = new Balancer(this.trainning, this.test, this.mut, this.iter);
				String results = b.balance();
				this.taMsgs.append(results);
			}
		}

		if(e.getSource() == btTrainning)
		{
			JFileChooser selectorArchivos = new JFileChooser();
			selectorArchivos.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int resultado = selectorArchivos.showOpenDialog(this);
			File source = selectorArchivos.getSelectedFile(); // obtiene el archivo seleccionado

			if (resultado == JFileChooser.APPROVE_OPTION)
			 {
				try (FileReader fr = new FileReader(source))
				{
					String cadena = "";
					int valor = fr.read();
					while (valor != -1)
					 {
						cadena = cadena + (char) valor;
						valor = fr.read();
					}
					tfTrainning.setText(source +"");
					tfTrainning.setEditable(false);
					this.trainning = cadena;

				}
				catch (IOException e1)	{e1.printStackTrace(); 	}
			}
		}

		if(e.getSource() == btTest)
		{
			JFileChooser selectorArchivos = new JFileChooser();
			selectorArchivos.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int resultado = selectorArchivos.showOpenDialog(this);
			File source = selectorArchivos.getSelectedFile(); // obtiene el archivo seleccionado

			if(resultado == JFileChooser.APPROVE_OPTION)
			{
				try(FileReader fr =new FileReader(source))
				{
					  String cadena="";
					  int valor=fr.read();
					  while(valor!=-1)
					  {
							cadena=cadena+(char)valor;
							valor=fr.read();
					  }
					  tfTest.setText(source +"");
					  tfTest.setEditable(false);
					  this.test = cadena;
				 }
				 catch (IOException e1) {e1.printStackTrace();}
			}
		}

		if(e.getSource() == btReset)
		{
			this.tfMutation.setEditable(true);
			this.tfIterations.setEditable(true);
			this.tfTrainning.setEditable(true);
			this.tfTest.setEditable(true);
			this.taMsgs.setText("");
		}
	}

	public boolean check()
	{
		boolean r = true;
		String failures = "";

		if(this.trainning.equals(""))
		{
			r = false;
			failures += "Set a trainning set.\n";
		}

		if(this.test.equals(""))
		{
			r = false;
			failures += "Set a test set.\n";
		}

		if(this.iterations.equals(""))
		{
			r = false;
			failures += "Set a number of iterations.\n";
		}
		else	if(isInteger(this.iterations))
			{this.iter = Integer.parseInt(this.iterations);}
		else {failures += "Set a valid integer for the number of iterations.\n"; }

		if(this.mutation.equals(""))
		{
			r = false;
			failures += "Set a % of mutation.\n";
		}
		else	if(isInteger(this.mutation))
			{this.mut = Integer.parseInt(this.mutation);}
		else {failures += "Set a valid % of mutation.\n"; }

		if(r == false)
			{ this.taMsgs.append("To balance the sample, please complete the following:\n" + failures); }

		return r;
	}

	public static boolean isInteger(String s)
	{
		boolean isValidInteger = false;
		try
		{
			if(Integer.parseInt(s) > 0) //&& Integer.parseInt(s)  < 100
				{isValidInteger = true;}// s is a valid integer
		}
		catch (NumberFormatException ex)
		{
			// s is not an integer
		}
		return isValidInteger;
	}

	public static void main(String [] args)
	{
		GUI_GA uI = new GUI_GA();
		uI.setVisible(true);
		uI.setSize(800, 250);
		uI.setDefaultCloseOperation(uI.EXIT_ON_CLOSE);
	}
}
