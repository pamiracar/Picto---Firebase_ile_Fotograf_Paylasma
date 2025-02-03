package com.pamiracar.picto.view

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.pamiracar.picto.databinding.FragmentYuklemeBinding
import java.util.UUID

class YuklemeFragment : Fragment() {

    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var db : FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        storage = Firebase.storage
        registerLaunchers()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.yukleButton.setOnClickListener {
            val uuid = UUID.randomUUID()//random isim
            val gorselAdi = "${uuid}.jpg"

            val reference = storage.reference
            val gorselReferansi = reference.child("images").child(gorselAdi)
            if (secilenGorsel != null){
                gorselReferansi.putFile(secilenGorsel!!).addOnSuccessListener { uploadTest ->
                    //Url'yi alma işlemi yapacağız

                    gorselReferansi.downloadUrl.addOnSuccessListener { uri ->
                        if(auth.currentUser != null){
                            val downloadUrl = uri.toString()
                            //veri tabanına kayıt yapmak gerekiyor
                            val postMap = hashMapOf<String, Any>()
                            postMap.put("downloadUrl",downloadUrl)
                            postMap.put("email",auth.currentUser?.email.toString())
                            postMap.put("comment",binding.aciklamaEditText.text.toString())
                            postMap.put("date", Timestamp.now())

                            db.collection("Posts").add(postMap).addOnSuccessListener { documentReference ->
                                //veri database ye yüklendi
                                val action = YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                                Navigation.findNavController(view).navigate(action)
                            }.addOnFailureListener { exception ->
                                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                            }

                        }


                    }


                }.addOnFailureListener{exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }

            }
        }

        binding.addPhotoIcon.setOnClickListener {



            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                //read media images
                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    //izin yok
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                        //izin mantığı gösterilmeli
                        Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                        ,View.OnClickListener {
                            //izin istenecek
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()
                    }else{
                        //izin istenecek
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }else{
                    /*
                    izin var
                    galeriye gitme kodu yazılacak
                     */
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }else{
                //read external storage - last checkpoint
                if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //izin yok
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)){
                        //izin mantığı gösterilmeli
                        Snackbar.make(view,"Galeriye gitmek için izin gerekli",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver"
                            ,View.OnClickListener {
                                //izin istenecek
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    }else{
                        //izin istenecek
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }else{
                    /*
                    izin var
                    galeriye gitme kodu yazılacak
                     */
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }
        }
    }

    private fun registerLaunchers() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        secilenGorsel = intentFromResult.data
                        try {
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    secilenGorsel!!
                                )
                                secilenBitmap = ImageDecoder.decodeBitmap(source)
                                binding.addPhotoIcon.setImageBitmap(secilenBitmap)
                            } else {
                                secilenBitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    secilenGorsel
                                )
                                binding.addPhotoIcon.setImageBitmap(secilenBitmap)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //izin verildi
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    //izin reddedildi
                    Toast.makeText(
                        requireContext(),
                        "İzni reddettiniz. İzne ihtiyacımız var",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}