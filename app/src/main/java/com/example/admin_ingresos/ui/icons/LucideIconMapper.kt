package com.example.admin_ingresos.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*

/**
 * Sistema de mapeo de iconos usando Lucide Icons
 * Proporciona iconos consistentes y profesionales para toda la aplicación
 */
object LucideIconMapper {
    
    /**
     * Mapea emojis de categorías a iconos Lucide profesionales
     */
    fun getIconFromEmoji(emoji: String): ImageVector {
        return when (emoji) {
            // Alimentación y restaurantes
            "🍕", "🍔", "🥘", "🍽️", "🥗", "🍎", "🥙", "🌮", "🥪" -> Lucide.UtensilsCrossed
            "☕", "🥤", "🍺", "🍷", "🧃" -> Lucide.Coffee
            "🛒", "🏪", "🛍️" -> Lucide.ShoppingCart
            
            // Transporte
            "🚗", "🚙", "🚘" -> Lucide.Car
            "⛽", "🛣️" -> Lucide.Fuel
            "🚌", "🚊", "🚇" -> Lucide.Bus
            "🚕", "🚖" -> Lucide.Car // Taxi
            "🚲", "🛴" -> Lucide.Bike
            "✈️", "🛫", "🛬" -> Lucide.Plane
            "🚢", "⛵" -> Lucide.Ship
            "🚂", "🚆", "🚄" -> Lucide.Zap // Using Zap as fallback for train
            
            // Entretenimiento
            "🎬", "🎭", "🎪" -> Lucide.Film
            "🎮", "🕹️" -> Lucide.Gamepad2
            "🎵", "🎶", "🎤" -> Lucide.Music
            "📺", "📻" -> Lucide.Tv
            "🎨", "🖼️" -> Lucide.Palette
            "📚", "📖", "📑" -> Lucide.Book
            "🏃", "⚽", "🏀", "🎾" -> Lucide.Dumbbell
            
            // Servicios y utilidades
            "💡", "🔌" -> Lucide.Zap
            "💧", "🚿" -> Lucide.Droplets
            "📶", "📱", "📞" -> Lucide.Smartphone
            "🌐", "💻", "🖥️" -> Lucide.Wifi
            "🔥", "❄️" -> Lucide.Thermometer
            "🗑️", "♻️" -> Lucide.Trash2
            
            // Compras y ropa
            "👕", "👔", "👗", "👠" -> Lucide.Shirt
            "🧥", "👖", "👒" -> Lucide.Shirt
            "💍", "⌚", "👜" -> Lucide.Gem
            
            // Salud y cuidado personal
            "🏥", "🏩" -> Lucide.Cross
            "💊", "🩺" -> Lucide.Pill
            "⚕️", "🔬" -> Lucide.Stethoscope
            "💉", "🩹" -> Lucide.Syringe
            "🦷", "🪥" -> Lucide.Smile
            "💄", "🧴", "🧼" -> Lucide.Sparkles
            
            // Educación y trabajo
            "📚", "📖", "📝" -> Lucide.BookOpen
            "🎓", "🏫", "📐" -> Lucide.GraduationCap
            "✏️", "🖊️", "📏" -> Lucide.PenTool
            "💼", "👔", "🏢" -> Lucide.Briefcase
            "💻", "⌨️", "🖥️" -> Lucide.Laptop
            
            // Hogar y mantenimiento
            "🏠", "🏡", "🏘️" -> Lucide.House
            "🔧", "🛠️", "🔨" -> Lucide.Wrench
            "🪑", "🛏️", "🚪" -> Lucide.Armchair
            "🧹", "🧽", "🧴" -> Lucide.Sparkles
            "🌱", "🌿", "🪴" -> Lucide.Leaf
            
            // Finanzas y dinero
            "💰", "💵", "💴", "💶", "💷" -> Lucide.DollarSign
            "💳", "💎" -> Lucide.CreditCard
            "🏦", "🏧" -> Lucide.Building2
            "📊", "📈", "📉" -> Lucide.TrendingUp
            "💸", "🪙" -> Lucide.Coins
            
            // Regalos y eventos
            "🎁", "🎉", "🎈" -> Lucide.Gift
            "🎂", "🍰", "🧁" -> Lucide.Cake
            "💝", "💌" -> Lucide.Heart
            "🎊", "🏆", "🥇" -> Lucide.Award
            
            // Tecnología
            "📱", "📞" -> Lucide.Smartphone
            "💻", "🖥️" -> Lucide.Monitor
            "⌚", "📟" -> Lucide.Watch
            "🖨️", "📠" -> Lucide.Printer
            "📷", "📹" -> Lucide.Camera
            "🎧", "🔊" -> Lucide.Headphones
            
            // Viajes y turismo
            "🧳", "🎒" -> Lucide.Luggage
            "🗺️", "🧭" -> Lucide.Map
            "🏨", "🏩" -> Lucide.Building
            "🏖️", "🏝️" -> Lucide.Trees
            "⛰️", "🏔️" -> Lucide.Mountain
            "🎪", "🎡", "🎢" -> Lucide.PartyPopper
            
            // Mascotas y animales
            "🐕", "🐶" -> Lucide.Dog
            "🐱", "🐈" -> Lucide.Cat
            "🐟", "🐠" -> Lucide.Fish
            "🐦", "🦜" -> Lucide.Bird
            
            // Seguros y emergencias
            "🚨", "🚑" -> Lucide.Siren
            "🔒", "🛡️" -> Lucide.Shield
            "⚠️", "🚧" -> Lucide.TriangleAlert
            
            // Default fallback
            else -> Lucide.Tag
        }
    }
    
    /**
     * Mapea nombres de categorías a iconos Lucide
     */
    fun getIconFromCategoryName(categoryName: String): ImageVector {
        return when (categoryName.lowercase()) {
            // Alimentación
            "alimentación", "comida", "restaurante", "supermercado", "mercado" -> Lucide.UtensilsCrossed
            "bebidas", "café", "bar" -> Lucide.Coffee
            
            // Transporte
            "transporte", "gasolina", "combustible" -> Lucide.Car
            "taxi", "uber", "cabify" -> Lucide.Car
            "bus", "metro", "transporte público" -> Lucide.Bus
            "avión", "vuelo", "aeropuerto" -> Lucide.Plane
            "bicicleta", "patinete" -> Lucide.Bike
            
            // Entretenimiento
            "entretenimiento", "diversión", "ocio" -> Lucide.Film
            "cine", "teatro", "espectáculos" -> Lucide.Film
            "videojuegos", "juegos" -> Lucide.Gamepad2
            "música", "conciertos" -> Lucide.Music
            "deportes", "gimnasio", "fitness" -> Lucide.Dumbbell
            "libros", "lectura" -> Lucide.Book
            
            // Servicios
            "servicios", "luz", "electricidad" -> Lucide.Zap
            "agua", "acueducto" -> Lucide.Droplets
            "internet", "telefonía", "móvil" -> Lucide.Wifi
            "gas", "calefacción" -> Lucide.Thermometer
            
            // Compras
            "compras", "ropa", "vestimenta" -> Lucide.Shirt
            "shopping", "centro comercial" -> Lucide.ShoppingCart
            "joyería", "accesorios" -> Lucide.Gem
            
            // Salud
            "salud", "médico", "hospital" -> Lucide.Cross
            "farmacia", "medicamentos" -> Lucide.Pill
            "dentista", "odontología" -> Lucide.Smile
            "belleza", "cosmética" -> Lucide.Sparkles
            
            // Educación
            "educación", "estudio", "universidad" -> Lucide.GraduationCap
            "cursos", "capacitación" -> Lucide.BookOpen
            "libros", "material de estudio" -> Lucide.Book
            
            // Trabajo
            "trabajo", "oficina", "negocio" -> Lucide.Briefcase
            "tecnología", "software" -> Lucide.Laptop
            
            // Hogar
            "hogar", "casa", "vivienda" -> Lucide.House
            "mantenimiento", "reparaciones" -> Lucide.Wrench
            "muebles", "decoración" -> Lucide.Armchair
            "jardinería", "plantas" -> Lucide.Leaf
            "limpieza" -> Lucide.Sparkles
            
            // Finanzas
            "finanzas", "banco", "inversión" -> Lucide.Building2
            "ahorros", "dinero" -> Lucide.DollarSign
            "tarjetas", "crédito" -> Lucide.CreditCard
            
            // Regalos
            "regalos", "obsequios" -> Lucide.Gift
            "celebraciones", "fiestas" -> Lucide.PartyPopper
            "cumpleaños" -> Lucide.Cake
            
            // Viajes
            "viajes", "turismo", "vacaciones" -> Lucide.Luggage
            "hotel", "hospedaje" -> Lucide.Building
            "mapas", "navegación" -> Lucide.Map
            
            // Seguros
            "seguros", "protección" -> Lucide.Shield
            "emergencias" -> Lucide.Siren
            
            // Mascotas
            "mascotas", "perros" -> Lucide.Dog
            "gatos" -> Lucide.Cat
            "veterinario" -> Lucide.Heart
            
            // Inversiones y negocios
            "inversiones", "acciones" -> Lucide.TrendingUp
            "criptomonedas", "crypto" -> Lucide.Coins
            
            // Default
            else -> Lucide.Tag
        }
    }
    
    /**
     * Obtiene el icono más apropiado basado en emoji o nombre de categoría
     */
    fun getCategoryIcon(category: com.example.admin_ingresos.data.Category): ImageVector {
        // Primero intenta obtener desde el emoji si existe
        if (category.icon.isNotEmpty()) {
            val iconFromEmoji = getIconFromEmoji(category.icon)
            if (iconFromEmoji != Lucide.Tag) {
                return iconFromEmoji
            }
        }
        
        // Si no hay emoji o no se encuentra, usa el nombre de la categoría
        return getIconFromCategoryName(category.name)
    }
    
    /**
     * Iconos para tipos de transacción
     */
    fun getTransactionTypeIcon(type: String): ImageVector {
        return when (type.lowercase()) {
            "ingreso", "income" -> Lucide.TrendingUp
            "gasto", "expense" -> Lucide.TrendingDown
            else -> Lucide.DollarSign
        }
    }
    
    /**
     * Iconos para métodos de pago
     */
    fun getPaymentMethodIcon(paymentMethod: String): ImageVector {
        return when (paymentMethod.lowercase()) {
            "efectivo", "cash" -> Lucide.Banknote
            "tarjeta de crédito", "crédito", "credit card" -> Lucide.CreditCard
            "tarjeta de débito", "débito", "debit card" -> Lucide.CreditCard
            "transferencia", "transfer", "banco" -> Lucide.Building2
            "nequi", "daviplata", "digital", "wallet" -> Lucide.Smartphone
            "paypal", "online" -> Lucide.Globe
            "cheque" -> Lucide.FileText
            else -> Lucide.Wallet
        }
    }
    
    /**
     * Método principal para obtener iconos de navegación por nombre
     */
    fun getNavigationIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "home", "casa" -> Lucide.House
            "transactions", "transacciones" -> Lucide.Receipt
            "reports", "reportes" -> Lucide.TrendingUp
            "settings", "configuracion" -> Lucide.Settings
            "profile", "perfil" -> Lucide.User
            "notifications", "notificaciones" -> Lucide.Bell
            "search", "buscar" -> Lucide.Search
            "filter", "filtrar" -> Lucide.Filter
            "add", "agregar" -> Lucide.Plus
            "edit", "editar" -> Lucide.PenTool
            "delete", "eliminar" -> Lucide.Trash2
            "back", "atras" -> Lucide.ArrowLeft
            "forward", "adelante" -> Lucide.ArrowRight
            "up", "arriba" -> Lucide.ChevronUp
            "down", "abajo" -> Lucide.ChevronDown
            "close", "cerrar" -> Lucide.X
            "check", "confirmar" -> Lucide.Check
            "calendar", "calendario" -> Lucide.Calendar
            "clock", "reloj" -> Lucide.Clock
            "info", "informacion" -> Lucide.Info
            "warning", "advertencia" -> Lucide.TriangleAlert
            "error" -> Lucide.CircleAlert
            "success", "exitoso" -> Lucide.CircleCheck
            "camera", "camara" -> Lucide.Camera
            "upload", "subir" -> Lucide.Upload
            "download", "descargar" -> Lucide.Download
            "share", "compartir" -> Lucide.Share
            "menu" -> Lucide.Menu
            "more", "mas" -> Lucide.Ellipsis
            "dollarSign", "dollar" -> Lucide.DollarSign
            "money" -> Lucide.Coins
            else -> Lucide.Tag
        }
    }
    
    /**
     * Método para obtener iconos disponibles para categorías
     */
    fun getAvailableCategoryIcons(): List<CategoryIconOption> {
        return listOf(
            CategoryIconOption("UtensilsCrossed", "Comida", "🍽️"),
            CategoryIconOption("Car", "Transporte", "🚗"),
            CategoryIconOption("House", "Hogar", "🏠"),
            CategoryIconOption("Briefcase", "Trabajo", "💼"),
            CategoryIconOption("GraduationCap", "Educación", "🎓"),
            CategoryIconOption("Cross", "Salud", "⚕️"),
            CategoryIconOption("Film", "Entretenimiento", "🎬"),
            CategoryIconOption("Shirt", "Ropa", "👕"),
            CategoryIconOption("DollarSign", "Finanzas", "💰"),
            CategoryIconOption("Gift", "Regalos", "🎁"),
            CategoryIconOption("Luggage", "Viajes", "🧳"),
            CategoryIconOption("Zap", "Servicios", "⚡"),
            CategoryIconOption("ShoppingCart", "Compras", "🛒"),
            CategoryIconOption("Dog", "Mascotas", "🐕"),
            CategoryIconOption("Dumbbell", "Deportes", "🏋️"),
            CategoryIconOption("Music", "Música", "🎵"),
            CategoryIconOption("Book", "Libros", "📚"),
            CategoryIconOption("Coffee", "Café", "☕"),
            CategoryIconOption("Gamepad2", "Juegos", "🎮"),
            CategoryIconOption("Shield", "Seguros", "🛡️")
        )
    }

    /**
     * Iconos para navegación y UI
     */
    object Navigation {
        val home = Lucide.House
        val transactions = Lucide.Receipt
        val reports = Lucide.TrendingUp
        val settings = Lucide.Settings
        val profile = Lucide.User
        val notifications = Lucide.Bell
        val search = Lucide.Search
        val filter = Lucide.Filter
        val add = Lucide.Plus
        val edit = Lucide.PenTool
        val delete = Lucide.Trash2
        val back = Lucide.ArrowLeft
        val forward = Lucide.ArrowRight
        val up = Lucide.ChevronUp
        val down = Lucide.ChevronDown
        val close = Lucide.X
        val check = Lucide.Check
        val calendar = Lucide.Calendar
        val clock = Lucide.Clock
        val info = Lucide.Info
        val warning = Lucide.TriangleAlert
        val error = Lucide.CircleAlert
        val success = Lucide.CircleCheck
        val camera = Lucide.Camera
        val upload = Lucide.Upload
        val download = Lucide.Download
        val share = Lucide.Share
        val menu = Lucide.Menu
        val more = Lucide.Ellipsis
    }
}

/**
 * Clase de datos para opciones de iconos de categoría
 */
data class CategoryIconOption(
    val name: String,
    val description: String,
    val icon: String
)

