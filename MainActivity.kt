package com.example.kalavidrabalaga

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppScreen()
        }
    }
}

data class Troupe(
    val id: String = "",
    val name: String = "",
    val artType: String = "",
    val district: String = "",
    val contact: String = "",
    val description: String = ""
)

@Composable
fun AppScreen() {
    var screen by remember { mutableStateOf("home") }
    var selectedTroupe by remember { mutableStateOf<Troupe?>(null) }
    val troupes = remember { mutableStateListOf<Troupe>() }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("troupes")
            .addSnapshotListener { result, _ ->
                if (result != null) {
                    troupes.clear()
                    for (doc in result.documents) {
                        troupes.add(
                            Troupe(
                                id = doc.id,
                                name = doc.getString("Name") ?: "",
                                artType = doc.getString("ArtType") ?: "",
                                district = doc.getString("District") ?: "",
                                contact = doc.getString("Contact") ?: "",
                                description = doc.getString("Description") ?: ""
                            )
                        )
                    }
                }
            }
    }

    androidx.activity.compose.BackHandler(enabled = screen != "home") {
        screen = when (screen) {
            "detail" -> "search"
            "register" -> "home"
            "search" -> "home"
            else -> "home"
        }
    }

    when (screen) {
        "home" -> HomeScreen(
            onFindArtists = { screen = "search" },
            onRegister = { screen = "register" }
        )
        "search" -> SearchScreen(
            troupes = troupes,
            onBack = { screen = "home" },
            onTroupeClick = {
                selectedTroupe = it
                screen = "detail"
            }
        )
        "detail" -> DetailScreen(
            troupe = selectedTroupe!!,
            onBack = { screen = "search" }
        )
        "register" -> RegisterScreen(
            onBack = { screen = "home" }
        )
    }
}

@Composable
fun HomeScreen(onFindArtists: () -> Unit, onRegister: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.folk),
            contentDescription = "Folk Dance",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33000000),
                            Color(0x33000000),
                            Color(0xEE000000)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "ಕಲಾವಿದರ ಬಳಗ",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Folk Artist Talent Hub",
                fontSize = 16.sp,
                color = Color(0xFFFFD9CC),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connecting Karnataka's traditional artists with event planners",
                fontSize = 13.sp,
                color = Color(0xFFFFD9CC),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onFindArtists,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8896A))
            ) {
                Text("🎭 Find Artists", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x55FFFFFF))
            ) {
                Text("📝 Register Your Troupe", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SearchScreen(
    troupes: List<Troupe>,
    onBack: () -> Unit,
    onTroupeClick: (Troupe) -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    val filtered = if (searchText.isEmpty()) troupes
    else troupes.filter {
        it.name.contains(searchText, ignoreCase = true) ||
                it.artType.contains(searchText, ignoreCase = true) ||
                it.district.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF3EC))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color(0xFF8B2500))
            }
            Text(text = "Find Artists", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B2500))
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search by name, art type, district...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (troupes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF8B2500))
            }
        } else {
            LazyColumn {
                items(filtered) { troupe ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onTroupeClick(troupe) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDE4))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = troupe.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF8B2500))
                            Text(text = troupe.artType, color = Color(0xFFE8896A), fontSize = 14.sp)
                            Text(text = "📍 ${troupe.district}", color = Color(0xFF888888), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailScreen(troupe: Troupe, onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3EC)).padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", color = Color(0xFF8B2500))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDE4))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = troupe.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B2500))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "🎭 Art Type: ${troupe.artType}", fontSize = 15.sp, color = Color(0xFF555555))
                Text(text = "📍 District: ${troupe.district}", fontSize = 15.sp, color = Color(0xFF555555))
                Text(text = "📞 Contact: ${troupe.contact}", fontSize = 15.sp, color = Color(0xFF555555))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = troupe.description, fontSize = 14.sp, color = Color(0xFF775533))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${troupe.contact}"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8896A))
        ) {
            Text("📞 Book Now", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun generateBio(name: String, artType: String, district: String, apiKey: String, onResult: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Write a professional bio for a Karnataka folk art troupe. Name: $name. Art Type: $artType. District: $district. Write one paragraph in English and one paragraph in Kannada.")
                            })
                        })
                    })
                })
            }.toString()

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)

            val result = if (json.has("candidates")) {
                json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else {
                "English: $name is a renowned $artType troupe from $district, Karnataka dedicated to preserving traditional folk arts.\n\nಕನ್ನಡ: $name ಒಂದು ಪ್ರಸಿದ್ಧ $artType ತಂಡವಾಗಿದ್ದು $district ಜಿಲ್ಲೆಯಿಂದ ಬಂದಿದೆ."
            }

            withContext(Dispatchers.Main) { onResult(result) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onResult("English: $name is a renowned $artType troupe from $district, Karnataka dedicated to preserving traditional folk arts.\n\nಕನ್ನಡ: $name ಒಂದು ಪ್ರಸಿದ್ಧ $artType ತಂಡವಾಗಿದ್ದು $district ಜಿಲ್ಲೆಯಿಂದ ಬಂದಿದೆ.")
            }
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var artType by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var generatedBio by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val apiKey = "AIzaSyCKMLNM_pbPNqe8yBY8KizTbs8T2lL0tGA"

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3EC)).padding(16.dp)
    ) {
        item {
            TextButton(onClick = onBack) {
                Text("← Back", color = Color(0xFF8B2500))
            }
            Text(text = "Register Your Troupe", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B2500))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Troupe Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = artType, onValueChange = { artType = it }, label = { Text("Art Type (e.g. Dollu Kunitha)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isGenerating = true
                    generatedBio = ""
                    generateBio(name, artType, district, apiKey) { bio ->
                        generatedBio = bio
                        isGenerating = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF775533))
            ) {
                Text("✨ Generate AI Bio", color = Color.White)
            }
            if (isGenerating) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8B2500))
                }
            }
            if (generatedBio.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDE4))
                ) {
                    Text(text = generatedBio, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Color(0xFF555555))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val troupe = hashMapOf(
                        "Name" to name,
                        "ArtType" to artType,
                        "District" to district,
                        "Contact" to contact,
                        "Description" to if (generatedBio.isNotEmpty()) generatedBio else description
                    )
                    db.collection("troupes").add(troupe)
                        .addOnSuccessListener {
                            successMessage = "✅ Troupe registered successfully!"
                            name = ""; artType = ""; district = ""; contact = ""; description = ""; generatedBio = ""
                        }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8896A))
            ) {
                Text("Register Troupe", color = Color.White, fontSize = 16.sp)
            }
            if (successMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = successMessage, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}