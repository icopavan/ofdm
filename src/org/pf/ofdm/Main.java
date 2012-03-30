package org.pf.ofdm;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class Main
{
	private static PseudoRNG generator = new PseudoRNG();

	final static int subcarriersCount = 30;					//subcarriers count
	final static double carrierFrequency = 2412E6;			//carrier frequency
	final static double subcarrierShift = 312.5E3;			//312.5 kHz subcarriers diversity (IEEE 802.11a)
	
	final static double initialQPSKShift = Math.PI / 4;		//initial QPSK symbol shift
	final static double qpskShift = Math.PI / 2;			//QPSK symbols shift step

	final static double subcarrierAmplitude = 1.0 / subcarriersCount;

	static double[] qpskSymbols = new double[subcarriersCount];

	private static boolean getRandomSymbol()
	{
		return generator.nextDouble() < 0.5 ? false : true;
	}

	private static double getRandomQPSKSymbol(boolean _s1, boolean _s2)
	{
		double symbolShift = 0;
		if (_s1 == false && _s2 == false)
			symbolShift = initialQPSKShift + 2 * qpskShift;
		else if (_s1 == false && _s2 == true)
			symbolShift = initialQPSKShift + 1 * qpskShift;
		else if (_s1 == true && _s2 == false)
			symbolShift = initialQPSKShift + 3 * qpskShift;
		else if (_s1 == true && _s2 == true)
			symbolShift = initialQPSKShift + 0 * qpskShift;
		return symbolShift;
	}
	
	private static double getOFDMAmplitude(double _t, double _shift)
	{
		double out = 0;
		for (int i = 0; i < subcarriersCount / 2; i++)
			out += subcarrierAmplitude * Math.cos(_shift - (2 * i + 1) * qpskSymbols[i] + (- (2 * i + 1) * subcarrierShift  + carrierFrequency) * _t);
		for (int i = 0; i < subcarriersCount / 2; i++)
			out += subcarrierAmplitude * Math.cos(_shift + (2 * i + 1) * qpskSymbols[i + subcarriersCount / 2] + ((2 * i + 1) * subcarrierShift + carrierFrequency) * _t);
		return out;
	}

	private static void regenerateQPSKSymbols()
	{
		try
		{
			FileWriter fw = new FileWriter("out.seq", true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < subcarriersCount; i++)
			{
				boolean s1 = getRandomSymbol();
				boolean s2 = getRandomSymbol();
				bw.write(s1 ? "1" : "0");
				bw.write(s2 ? "1" : "0");
				qpskSymbols[i] = getRandomQPSKSymbol(s1, s2);
			}
			bw.write("\n");
			bw.close();
			fw.close();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static void main(String[] argv)
	{
		
		final double simulationTime = 2E-5;

		final double simulationTimeStep = 1 / (2 * (carrierFrequency + subcarriersCount * subcarrierShift + 2 * Math.PI)); //hello, Kotelnikov
		final int ansamblesCount = (int)(simulationTime / simulationTimeStep); 
		
		double[] ansamble = new double[ansamblesCount + 1];

		try
		{
			FileWriter fw = new FileWriter("out.seq");
			BufferedWriter bw = new BufferedWriter(fw);
			bw.close();
			fw.close();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}

		//regenerateQPSKSymbols(); // --uncomment to disable bit sequence regenerating
		final int ofdmSymbolSteps = 5000;
		System.out.printf("Sim. time: %E\nSim. time step: %E\nSim. steps: %f\nOFDM symbol duration: %E\n", simulationTime, simulationTimeStep, simulationTime / simulationTimeStep, simulationTimeStep * ofdmSymbolSteps);
		
		//build OFDM ansamble
		try
		{
			FileWriter fw = new FileWriter("out.res");
			BufferedWriter bw = new BufferedWriter(fw);

			int index = 0;
			
			for (double t = 0; t < simulationTime; t += simulationTimeStep)
			{
				if (index % ofdmSymbolSteps == 0) // --comment to disable bit sequence regenerating
					regenerateQPSKSymbols();      // --comment to disable bit sequence regenerating

				double y = getOFDMAmplitude(t, 0);
				double ys = getOFDMAmplitude(t, Math.PI / 2);
				double A = Math.sqrt(Math.pow(y, 2) + Math.pow(ys, 2)); //hello, Hilbert
				ansamble[index++] = A;
				bw.write(t + "\t" + A + "\n");
			}
			
			bw.close();
			fw.close();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		//find average and maximum value
		double avg = 0, max = ansamble[0];
		for (double cd: ansamble)
		{
			if (cd > max)
				max = cd;
			avg += cd / ansamble.length;
		}

		//PAPR
		double papr = max / avg;
		System.out.printf("PAPR=%f\n", papr);
	}
}
