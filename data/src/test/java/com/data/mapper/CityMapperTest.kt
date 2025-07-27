import com.data.dto.CityRemoteDto
import com.data.dto.CoordinatesDto
import com.data.mapper.CityMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CityMapperTest {

    private lateinit var mapper: CityMapper

    @BeforeEach
    fun setup() {
        mapper = CityMapper()
    }

    @Test
    fun `mapToDomain should convert DTO to domain model correctly`() {
        // Given
        val dto = CityRemoteDto(
            _id = 1,
            name = "Bogotá",
            country = "Colombia",
            coordinates = CoordinatesDto(lat = 4.7110, lon = -74.0721)
        )

        // When
        val result = mapper.mapToDomain(dto, isFavorite = true)

        // Then
        assertEquals(1, result.id)
        assertEquals("Bogotá", result.name)
        assertEquals("Colombia", result.country)
        assertEquals(4.7110, result.lat, 0.001)
        assertEquals(-74.0721, result.lon, 0.001)
        assertEquals(true, result.isFavorite)
    }

    @Test
    fun `mapToDomain should set isFavorite to false by default`() {
        // Given
        val dto = CityRemoteDto(
            _id = 2,
            name = "Medellín",
            country = "Colombia",
            coordinates = CoordinatesDto(lat = 6.2442, lon = -75.5812)
        )

        // When
        val result = mapper.mapToDomain(dto)

        // Then
        assertEquals(false, result.isFavorite)
    }

    @Test
    fun `mapListToDomain should convert list of DTOs to domain models`() {
        // Given
        val dtos = listOf(
            CityRemoteDto(1, "Bogotá", "Colombia", CoordinatesDto(4.7110, -74.0721)),
            CityRemoteDto(2, "Medellín", "Colombia", CoordinatesDto(6.2442, -75.5812))
        )
        val favoriteIds = setOf(1)

        // When
        val result = mapper.mapListToDomain(dtos, favoriteIds)

        // Then
        assertEquals(2, result.size)
        assertEquals(true, result[0].isFavorite)
        assertEquals(false, result[1].isFavorite)
        assertEquals("Bogotá", result[0].name)
        assertEquals("Medellín", result[1].name)
    }

    @Test
    fun `mapListToDomain should handle empty list`() {
        // Given
        val dtos = emptyList<CityRemoteDto>()
        val favoriteIds = emptySet<Int>()

        // When
        val result = mapper.mapListToDomain(dtos, favoriteIds)

        // Then
        assertEquals(0, result.size)
    }
}
