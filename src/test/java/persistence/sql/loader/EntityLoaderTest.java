package persistence.sql.loader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.TestUtils;
import persistence.config.TestPersistenceConfig;
import persistence.proxy.ProxyFactory;
import persistence.sql.common.util.CamelToSnakeConverter;
import persistence.sql.dml.Database;
import persistence.sql.dml.TestEntityInitialize;
import persistence.sql.dml.impl.SimpleMetadataLoader;
import persistence.sql.fixture.TestOrder;
import persistence.sql.fixture.TestPerson;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("EntityLoader 테스트")
class EntityLoaderTest extends TestEntityInitialize {
    private Database database;
    private EntityLoader<TestPerson> loader;
    private EntityLoader<TestOrder> orderLoader;
    private ProxyFactory proxyFactory;

    @BeforeEach
    void setUp() throws SQLException {
        TestPersistenceConfig config = TestPersistenceConfig.getInstance();
        proxyFactory = config.proxyFactory();
        database = config.database();

        loader = new EntityLoader<>(TestPerson.class, database, proxyFactory);
        orderLoader = new EntityLoader<>(TestOrder.class, database, proxyFactory);

        database.executeUpdate("INSERT INTO users (nick_name, old, email) VALUES ('catsbi', 55, 'catsbi@naver.com')");
        database.executeUpdate("INSERT INTO users (nick_name, old, email) VALUES ('crong', 7, 'crong@naver.com')");
        database.executeUpdate("INSERT INTO users (nick_name, old, email) VALUES ('pobi', 66, 'pobi@gmail.com')");
        database.executeUpdate("INSERT INTO users (nick_name, old, email) VALUES ('navicat', 32, 'navi@hanmail.net')");

        database.executeUpdate("INSERT INTO orders (order_number) VALUES ('1')");
        database.executeUpdate("INSERT INTO order_items (product, quantity, order_id) VALUES ('apple', 10, 1)");
    }

    @Test
    @DisplayName("생성자는 기본 메타데이터 로더와 이름 변환기를 이용해 객체를 생성할 수 있다.")
    void testConstructor() throws NoSuchFieldException, IllegalAccessException {
        // when
        EntityLoader<TestPerson> loader = new EntityLoader<>(TestPerson.class, database, proxyFactory);

        Object actualNameConverter = TestUtils.getValueByFieldName(loader, "nameConverter");

        // then
        assertAll(
                () -> assertThat(loader).isNotNull(),
                () -> assertThat(loader.getMetadataLoader()).isInstanceOf(SimpleMetadataLoader.class),
                () -> assertThat(actualNameConverter).isInstanceOf(CamelToSnakeConverter.class)
        );
    }

    @Test
    @DisplayName("loadAll 함수를 통해 모든 엔티티를 조회할 수 있다.")
    void testLoadAll() {
        // when
        TestPerson[] actual = loader.loadAll().toArray(TestPerson[]::new);

        // then
        assertAll(
                () -> assertThat(actual).hasSize(4),
                () -> assertThat(actual[0].getName()).isEqualTo("catsbi"),
                () -> assertThat(actual[1].getName()).isEqualTo("crong"),
                () -> assertThat(actual[2].getName()).isEqualTo("pobi"),
                () -> assertThat(actual[3].getName()).isEqualTo("navicat")
        );
    }

    @Test
    @DisplayName("load 함수를 통해 특정 엔티티를 조회할 수 있다.")
    void testLoad() {
        // when
        TestPerson actual = loader.load(1L);

        // then
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getName()).isEqualTo("catsbi"),
                () -> assertThat(actual.getAge()).isEqualTo(55),
                () -> assertThat(actual.getEmail()).isEqualTo("catsbi@naver.com")
        );
    }

    @Test
    @DisplayName("load 함수를 통해 연관관계가 있는 특정 엔티티를 조회할 수 있다.")
    void testLoadWithAssociation() {
        // when
        TestOrder actual = orderLoader.load(1L);

        // then
        assertAll(
                () -> assertThat(actual).isNotNull(),
                () -> assertThat(actual.getOrderNumber()).isEqualTo("1")
        );
    }
}
