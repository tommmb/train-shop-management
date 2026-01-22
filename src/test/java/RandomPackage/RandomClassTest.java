package RandomPackage;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import RandomPackage.RandomClass;

public class RandomClassTest {
  @Test 
  public void addTestPositive() {
    RandomClass randomClass = new RandomClass();
    assertThat(randomClass.add(1,2)).isEqualTo(3);
  }

  @Test
  public void addTestNegative() {
    RandomClass randomClass = new RandomClass();
    assertThat(randomClass.add(-1,-2)).isEqualTo(-3);
  }
}