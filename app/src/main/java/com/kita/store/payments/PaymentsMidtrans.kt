package com.kita.store.payments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.kita.store.GlobalData
import com.kita.store.MainActivity
import com.kita.store.R
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pesan.*

class PaymentsMidtrans : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pesan)
        name.text = GlobalData.names
        harga.text = "Rp" + GlobalData.hargas.toString()
        deskripsi.text = GlobalData.deskripsis
        Picasso.get().load(GlobalData.photos).into(image)

        SdkUIFlowBuilder.init()

            .setClientKey("SB-Mid-client-YML3G1QwfLpvN0f3")
            .setContext(applicationContext)
            .setMerchantBaseUrl("http://192.168.17.150/store/response.php/")
            .setTransactionFinishedCallback(TransactionFinishedCallback {
                    result ->
                if(result.status == "success"){
                    //Toast.makeText(this, "Transaction Finished. ID: " + result.getResponse().getTransactionId(), Toast.LENGTH_LONG).show();
                    Toast.makeText(applicationContext, "Transaksi berhasil, Terimakasih", Toast.LENGTH_LONG).show()
                    kirimData(GlobalData.email, GlobalData.jumlah, GlobalData.catatan, GlobalData.names)
                }
            })
            .enableLog(true)
            .setColorTheme(CustomColorTheme("#FFE51255", "#B61548", "#FFE51255"))
            .setLanguage("id")
            .buildSDK()


        pesan.setOnClickListener {

            val hargaproduct = GlobalData.hargas
            val edittextHarga = jumlah.text.toString()
            val catatan = catatan.text.toString()
            GlobalData.jumlah = edittextHarga.toInt()
            GlobalData.catatan = catatan.toString()
            val convertharga = edittextHarga.toInt()
            val kalikan = convertharga * hargaproduct.toDouble()

            val transactionRequest = TransactionRequest("Kita-Store-"+System.currentTimeMillis().toString() + "", kalikan)
            val detail = com.midtrans.sdk.corekit.models.ItemDetails(""+GlobalData.ids, GlobalData.hargas.toDouble(), edittextHarga.toInt(), ""+GlobalData.names)
            val itemDetails = ArrayList<com.midtrans.sdk.corekit.models.ItemDetails>()
            itemDetails.add(detail)

            uiKitDetails(transactionRequest)
            transactionRequest.itemDetails = itemDetails
            MidtransSDK.getInstance().transactionRequest = transactionRequest
            MidtransSDK.getInstance().startPaymentUiFlow(this)

        }

    }




    fun uiKitDetails(transactionRequest: TransactionRequest){

        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = GlobalData.email
        customerDetails.email = GlobalData.email

        val shippingAddress = ShippingAddress()

        customerDetails.shippingAddress = shippingAddress
        val billingAddress = BillingAddress()

        customerDetails.billingAddress = billingAddress

        transactionRequest.customerDetails = customerDetails

    }
    fun kirimData(str:String, jml:Int, catatan:String, namas_products:String){

        var registerUrl:String = "http://192.168.17.150/store/historyorder.php"

        var request: RequestQueue = Volley.newRequestQueue(applicationContext)
        var strRequest = StringRequest(Request.Method.GET,registerUrl+"?id_user="+str.toString()+"&jumlah="+jml.toInt()+
                "&catatan="+catatan.toString()+"&nama_product="+namas_products.toString(), Response.Listener { response ->

            if (response.equals("1")){
                var i = Intent(this, MainActivity::class.java)
                startActivity(i)
            }else{
                Toast.makeText(applicationContext, "Ada yang salah, ulangi lagi", Toast.LENGTH_LONG).show();
            }

        }, Response.ErrorListener { error ->
            Log.d("ErrorApps", error.toString())
        })

        request.add(strRequest)
    }
    fun onTransactionFinished(result: TransactionResult) {
        if (result.response != null) {
            when (result.status) {
                TransactionResult.STATUS_SUCCESS -> Toast.makeText(
                    this,
                    "Transaction Finished. ID: " + result.response.transactionId,
                    Toast.LENGTH_LONG
                ).show()
                TransactionResult.STATUS_PENDING -> Toast.makeText(
                    this,
                    "Transaction Pending. ID: " + result.response.transactionId,
                    Toast.LENGTH_LONG
                ).show()
                TransactionResult.STATUS_FAILED -> Toast.makeText(
                    this,
                    "Transaction Failed. ID: " + result.response.transactionId + ". Message: " + result.response.statusMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            result.response.validationMessages
        } else if (result.isTransactionCanceled) {
            Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show()
        } else {
            if (result.status.equals(TransactionResult.STATUS_INVALID, ignoreCase = true)) {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

