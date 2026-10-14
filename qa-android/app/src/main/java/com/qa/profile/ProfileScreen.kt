package com.qa.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Warna mengikuti gambar soal. */
private val Pink = Color(0xFFDD2060)
private val Border = Color(0xFFBDBDBD)
private val Hint = Color(0xFF8A94A6)
private val ErrorRed = Color(0xFFD32F2F)

/** testTag dipakai tes UI; jangan diubah tanpa menyesuaikan androidTest. */
object Tags {
    const val Back = "back"
    const val FirstName = "firstName"
    const val MiddleName = "middleName"
    const val LastName = "lastName"
    const val Nickname = "nickname"
    const val Male = "genderMale"
    const val Female = "genderFemale"
    const val BirthDate = "birthDate"
    const val Save = "save"
    const val Message = "message"
    fun error(field: Field) = "error_${field.name}"
}

@Composable
fun ProfileScreen(vm: ProfileViewModel, onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag(Tags.Back)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Input your profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))

        LabeledField("First Name", false, vm.firstName, "e.g Joel", Tags.FirstName, vm.errors[Field.FirstName], Field.FirstName, vm::onFirstName)
        LabeledField("Middle Name", true, vm.middleName, "e.g K", Tags.MiddleName, vm.errors[Field.MiddleName], Field.MiddleName, vm::onMiddleName)
        LabeledField("Last Name", true, vm.lastName, "e.g Osteen", Tags.LastName, vm.errors[Field.LastName], Field.LastName, vm::onLastName)
        LabeledField("Nickname", false, vm.nickname, "Nickname", Tags.Nickname, vm.errors[Field.Nickname], Field.Nickname, vm::onNickname)

        Text("Gender", fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(72.dp)) {
            GenderOption("Male", vm.gender == Gender.Male, Tags.Male) { vm.onGender(Gender.Male) }
            GenderOption("Female", vm.gender == Gender.Female, Tags.Female) { vm.onGender(Gender.Female) }
        }

        LabeledField(
            "Birth Date", false, vm.birthDate, "DD MMM YYYY", Tags.BirthDate,
            vm.errors[Field.BirthDate], Field.BirthDate, vm::onBirthDate,
            leading = { Icon(Icons.Default.DateRange, contentDescription = null, tint = Hint) },
        )

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = vm::onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag(Tags.Save),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Pink, contentColor = Color.White),
        ) { Text("Save", fontWeight = FontWeight.Bold) }

        vm.message?.let {
            Text(it, color = Color(0xFF067D3C), fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp).testTag(Tags.Message))
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    optional: Boolean,
    value: String,
    placeholder: String,
    tag: String,
    error: String?,
    field: Field,
    onChange: (String) -> Unit,
    leading: (@Composable () -> Unit)? = null,
) {
    Row(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 12.sp)
        if (optional) {
            Spacer(Modifier.width(4.dp))
            Text("(Optional)", fontSize = 10.sp, color = Hint)
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().testTag(tag),
        singleLine = true,
        isError = error != null,
        placeholder = { Text(placeholder, color = Hint, fontSize = 13.sp) },
        leadingIcon = leading,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Pink, unfocusedBorderColor = Border, errorBorderColor = ErrorRed,
        ),
    )
    if (error != null) {
        Text(error, color = ErrorRed, fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp).testTag(Tags.error(field)))
    }
}

@Composable
private fun GenderOption(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .testTag(tag)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Pink))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp)
    }
}
