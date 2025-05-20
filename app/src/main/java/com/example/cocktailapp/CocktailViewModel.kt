import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cocktailapp.Cocktail
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CocktailViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cocktails = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktails: StateFlow<List<Cocktail>> = _cocktails

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites

    private val _selectedCocktail = MutableStateFlow<Cocktail?>(null)
    val selectedCocktail: StateFlow<Cocktail?> = _selectedCocktail

    private val _favoritesLoaded = MutableStateFlow(false)
    val favoritesLoaded: StateFlow<Boolean> = _favoritesLoaded

    private val _showLoading = MutableStateFlow(true)
    //val showLoading: StateFlow<Boolean> = _showLoading

    val splashStartTime = System.currentTimeMillis()


    init {
        waitForLoginAndListenToFavorites()
        loadCocktails()
        //listenToFavorites()
    }

    private fun loadCocktails() {
        db.collection("cocktails")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val cocktailsList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Cocktail::class.java)?.copy(id = doc.id)
                    }
                    _cocktails.value = cocktailsList
                }
            }
    }

    private fun refreshCocktail(id: String) {
        db.collection("cocktails").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val updated = document.toObject(Cocktail::class.java)?.copy(id = id)
                    updated?.let {
                        // zamiana w liście
                        _cocktails.value = _cocktails.value.map { old ->
                            if (old.id == id) it else old
                        }
                        _selectedCocktail.value = it
                    }
                }
            }
    }
    fun loadCocktail(id: String) {
        db.collection("cocktails").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val cocktail = document.toObject(Cocktail::class.java)
                    cocktail?.let {
                        _selectedCocktail.value = it.copy(id = document.id)
                    }
                }
            }
    }

    fun waitForLoginAndListenToFavorites() {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                listenToFavorites()
            }
        }
    }


    private fun listenToFavorites() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val favoriteIds = snapshot.documents.map { it.id }.toSet()
                    _favorites.value = favoriteIds
                    _favoritesLoaded.value = true

                    viewModelScope.launch {
                        delay(500)
                        _showLoading.value = false
                    }

                }
            }
    }

    fun toggleFavorite(cocktail: Cocktail) {
        val userId = auth.currentUser?.uid ?: return
        val favRef = db.collection("users").document(userId)
            .collection("favorites").document(cocktail.id)
        val cocktailRef = db.collection("cocktails").document(cocktail.id)

        if (_favorites.value.contains(cocktail.id)) {
            favRef.delete()
            cocktailRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .addOnSuccessListener {
                    refreshCocktail(cocktail.id)
                }
        } else {
            favRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
            cocktailRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(1))
                .addOnSuccessListener {
                    refreshCocktail(cocktail.id)
                }
        }
    }

}
