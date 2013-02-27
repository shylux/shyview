package shyview;

import javax.sound.sampled.*;

public class Sound implements Runnable{
	private static final long serialVersionUID = 1L;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			int sampleRate = 22050;
			AudioFormat audioformat =
				new AudioFormat(
					AudioFormat.Encoding.PCM_SIGNED,
					sampleRate,
					16,
					2,
					4,
					sampleRate,
					false);
			DataLine.Info datalineinfo =
				new DataLine.Info(SourceDataLine.class, audioformat);
			if (!AudioSystem.isLineSupported(datalineinfo)) {
				System.out.println(
					"Line matching " + datalineinfo + " is not supported.");
			} else {
				SourceDataLine sourcedataline =
					(SourceDataLine) AudioSystem.getLine(datalineinfo);
				sourcedataline.open(audioformat);
				sourcedataline.start();
				byte[] samples = new byte[1000];
				//for (int s = 1; s < 10; s++) {
				//int s = 1;
				int s = 1;
					for (int freq2 = 1000; freq2 < 1030; freq2 += s) {
						float size = ((float) sampleRate) / ((float) 1000);
						float amplitude = 32000;
						int adr = 0;
						for (int i = 0; i < size; i++, adr += 4) {
							double sin =
								Math.sin(
									(double) i / (double) size * 2.0 * Math.PI);
							int sample = (int) (sin * amplitude);
							samples[adr + 0] = (byte) (sample);
							samples[adr + 1] = (byte) (sample >>> 8);
							samples[adr + 2] = (byte) (sample);
							samples[adr + 3] = (byte) (sample >>> 8);
						}
						sourcedataline.write(samples, 0, adr);
					}
				//}
				sourcedataline.drain();
				sourcedataline.stop();
				sourcedataline.close();
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
}
