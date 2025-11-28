package com.example.personality_test

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore


data class TestQuestion(
    val title: String,
    val question: String,
    val selects: List<String>,
    val answers: List<String>
)



// 1) MBTI (I/E)
val mbtiTest = TestQuestion(
    title = "5초 MBTI I/E 편",
    question = "친구와 함께 간 미술관 당신이라면",
    selects = listOf(
        "말이 많아짐",
        "생각이 많아짐"
    ),
    answers = listOf(
        "당신의 성향은 E",
        "당신의 성향은 I"
    )
)

// 2) 당신이 좋아하는 애완동물은 (test1)
val animalTest = TestQuestion(
    title = "당신이 좋아하는 애완동물은",
    question = "당신이 무인도에 도착했는데 마침 떠내려온 상자를 열었을때 보이는 이것은",
    selects = listOf(
        "생존키트",
        "휴대폰",
        "텐트",
        "무인도에서 살아남기"
    ),
    answers = listOf(
        "당신은 현실주의 동물은 안키운다!!",
        "당신은 늘 함께 있는 걸 좋아하는 강아지가 딱입니다",
        "당신은 같은 공간을 공유하는 고양이",
        "당신은 낭만을 좋아하는 앵무새"
    )
)

// 3) 당신은 어떤 사랑을 하고 싶나요 (test2)
val loveTest = TestQuestion(
    title = "당신은 어떤 사랑을 하고 싶나요",
    question = "목욕을 할때 가장 먼저 비누칠을 하는 곳은",
    selects = listOf(
        "머리",
        "상체",
        "하체"
    ),
    answers = listOf(
        "당신은 자만추를 추천해요",
        "당신은 소개팅을 통한 새로운 사람의 소개를 좋아합니다",
        "당신은 길가다가 우연히 지나친 그런 인연을 좋아합니다"
    )
)

// 전체 테스트 리스트
val allTests = listOf(
    mbtiTest,
    loveTest,
    animalTest
)

class MainActivity : ComponentActivity() {

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PersonalityApp(
                tests = allTests,
                onSaveResult = { test, index ->
                    saveResultToFirestore(test, index)
                }
            )
        }
    }

    private fun saveResultToFirestore(test: TestQuestion, index: Int) {
        val selectedText = test.selects[index]
        val resultText = test.answers[index]

        val data = hashMapOf(
            "title" to test.title,
            "question" to test.question,
            "selectedIndex" to index,
            "selectedText" to selectedText,
            "resultText" to resultText,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("results")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "저장 완료! ($resultText)",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "저장 실패: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}

@Composable
fun PersonalityApp(
    tests: List<TestQuestion>,
    onSaveResult: (TestQuestion, Int) -> Unit
) {
    var selectedTestIndex by remember { mutableStateOf<Int?>(null) }
    var lastResult by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedTestIndex == null) {
            // 테스트 목록 화면
            TestListScreen(
                tests = tests,
                onClickTest = { index ->
                    selectedTestIndex = index
                    lastResult = null
                }
            )
        } else {
            // 개별 테스트 화면
            val test = tests[selectedTestIndex!!]
            TestQuestionScreen(
                test = test,
                lastResult = lastResult,
                onSelect = { answerIndex ->
                    onSaveResult(test, answerIndex)
                    lastResult = test.answers[answerIndex]
                },
                onBack = {
                    selectedTestIndex = null
                }
            )
        }
    }
}

@Composable
fun TestListScreen(
    tests: List<TestQuestion>,
    onClickTest: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "심리테스트 목록")
        Spacer(modifier = Modifier.height(24.dp))

        tests.forEachIndexed { index, test ->
            Button(
                onClick = { onClickTest(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text = test.title)
            }
        }
    }
}

@Composable
fun TestQuestionScreen(
    test: TestQuestion,
    lastResult: String?,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = test.title)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = test.question)
        Spacer(modifier = Modifier.height(24.dp))

        // 선택지 수만큼 버튼 만들기
        test.selects.forEachIndexed { index, text ->
            Button(
                onClick = { onSelect(index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(text)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (lastResult != null) {
            Text(text = "결과: $lastResult")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBack) {
            Text(text = "← 목록으로 돌아가기")
        }
    }
}
