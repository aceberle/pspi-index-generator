package driver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.arakelian.faker.model.Person;
import com.arakelian.faker.service.RandomData;
import com.arakelian.faker.service.RandomPerson;

public class GenerateDataDriver {

	@Test
	public void makeTestData() throws IOException {
		final File testDir = new File("src/test/directories/volume2");
		testDir.mkdirs();
		final List<Person> teachers = RandomPerson.get().listOf(3);
		final BufferedImage smallImage = new BufferedImage(320, 400, BufferedImage.TYPE_INT_RGB);
		final BufferedImage largeImage = new BufferedImage(640, 800, BufferedImage.TYPE_INT_RGB);
		final BufferedImage otherImage = new BufferedImage(800, 1000, BufferedImage.TYPE_INT_RGB);
		final BufferedImage badImage = new BufferedImage(850, 1000, BufferedImage.TYPE_INT_RGB);
		final BufferedImage[] images = new BufferedImage[] { smallImage, largeImage, otherImage, badImage };
		final Random random = new Random();
		for (Person teacher : teachers) {
			final String grade = nextGrade();
			final String homeRoomName = camel(teacher.getLastName());
			System.out.println(String.format("Teacher: %s Grade: %s", homeRoomName, grade));
			final String folderName = String.format("%s_%s", homeRoomName, grade);
			final File imageFolder = new File(testDir, folderName);
			imageFolder.mkdirs();
			final List<Person> students = RandomPerson.get().listOf(5);
			for (Person student : students) {
				final String firstName = camel(student.getFirstName());
				final String lastName = camel(student.getLastName());
				System.out.println(String.format("Student: %s %s", firstName, lastName));
				final String imageFileName = String.format("%s_%s.jpg", firstName, lastName);
				final File imageFile = new File(imageFolder, imageFileName);
				final BufferedImage image = images[random.nextInt(images.length)];
				System.out.println("Writing image to: " + imageFile);
				ImageIO.write(image, "jpg", imageFile);
			}
		}
	}

	private String camel(String string) {
		final int length = string.length();
		return length > 0
				? length > 1 ? string.substring(0, 1).toUpperCase().concat(string.substring(1).toLowerCase())
						: string.substring(0, 1)
				: string;
	}

	private String nextGrade() {
		return RandomData.get().nextString("grade");
	}

}
