package com.example.composecharactersbase

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Data models for the character API response
data class CharacterResponse(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val image: String
)

interface RickAndMortyApi {
    @GET("character/{id}")
    suspend fun getCharacter(@Path("id") id: Int): CharacterResponse
}

class CharacterRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(RickAndMortyApi::class.java)

    suspend fun getCharacter(id: Int): CharacterResponse {
        return api.getCharacter(id)
    }
}

@Composable
fun CharacterApp(viewModel: CharacterViewModel = CharacterViewModel(CharacterRepository())) {
    CharacterListScreen(viewModel)
}

@Composable
fun CharacterListScreen(viewModel: CharacterViewModel) {
    val characters by viewModel.characters.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(characters) { character ->
            CharacterCard(character)
        }
    }
}

@Composable
fun CharacterCard(character: CharacterResponse) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = character.image),
                contentDescription = "Character image",
                modifier = Modifier
                    .size(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = character.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "Status: ${character.status}")
                Text(text = "Species: ${character.species}")
            }

            IconButton(onClick = {
                isFavorite = !isFavorite
            }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) Color.Yellow else Color.Gray
                )
            }
        }
    }
}

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {
    private val _characters = MutableStateFlow<List<CharacterResponse>>(emptyList())
    val characters: StateFlow<List<CharacterResponse>> = _characters

    init {
        loadCharacters()
    }

    private fun loadCharacters() {
        viewModelScope.launch(Dispatchers.IO) {
            val characterList = mutableListOf<CharacterResponse>()
            for (id in 1..10) {
                val character = repository.getCharacter(id)
                characterList.add(character)
            }
            _characters.value = characterList
        }
    }
}
