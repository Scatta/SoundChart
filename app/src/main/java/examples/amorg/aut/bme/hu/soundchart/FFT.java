package examples.amorg.aut.bme.hu.soundchart;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Created by SlimShady on 2015.04.01..
 */

public class FFT {

    private int fftSize = 1024;
    private double[] fftData;
    private double[] temp;
    private double[] fftBuffer;
    private double[] spektrum;
    private double[] spektrum_komplex;
    private DoubleFFT_1D fft1d;

    public FFT (){
    };

    private double[] byteToDouble(byte[] bytes){
        double temp=0;
        double[] out = new double[bytes.length/4];
        for(int i = 0; i < bytes.length; i += 4){
            temp = bytes[i] &0xff;
            temp += (bytes[i+1]<<8 &0xff00);
            temp += (bytes[i+2]<<16 &0xff0000);
            temp += (bytes[i+3]<<24 &0xff000000);

            out[i/4] = (double) temp/Math.pow(2, 31);

        }
        return out;
    }

    public double[] doFFT(byte[] inputData){

        fftData = byteToDouble(inputData);
        temp = new double[fftSize];
        fftBuffer = new double[fftSize*2];
        spektrum = new double[fftSize+1];
        spektrum_komplex = new double[fftSize*2];

        fft1d = new DoubleFFT_1D(fftSize);

        for(int i=0; i<=fftData.length/fftSize-1;i++){
            for(int x=0; x<fftSize; x++){
                if(i==0){
                    temp[x] = fftData[x];
                }
                else{
                    temp[x] = fftData[i*fftSize+x];
                }
            }

            for(int y=0; y<fftSize*2; y+=2){
                fftBuffer[y] = temp[y/2];
                fftBuffer[y+1] = 0;
            }

            fft1d.complexForward(fftBuffer);

            for(int x=0; x<fftSize*2; x++){
                spektrum_komplex[x] = (spektrum_komplex[x]*(i)+fftBuffer[x])/(i+1); //átlagolás
            }
        }

        for(int i=0; i<fftBuffer.length; i += 2){
            spektrum[i/2] = Math.sqrt(Math.pow(spektrum_komplex[i],2) + Math.pow(spektrum_komplex[i+1],2)); //abszolút érték
        }

        return spektrum;

    }

    public double findMax(double[] buffer) {
        double max = buffer[0];
        int index = 0;
        for (int i = 1; i < buffer.length / 2; i++) {
            if (buffer[i] > max) {
                max = buffer[i];
                index = i;
            }
        }
        return index;
    }

    public int getFftSize() {
        return fftSize;
    }
}
